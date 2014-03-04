package net.fawad.rabbitmqloadgen

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import javax.xml.bind.DatatypeConverter
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.BasicProperties.Builder

class RabbitMQMessageReader {
  val bodyKey = "Body"
  val headersKey = "Headers"
  val propertiesToSkip = List(bodyKey, headersKey) ::: List("Acknowledger")
  val propertiesSetters = Map(
    "Priority" -> ((b: Builder, v: Any) => b.priority(v.asInstanceOf[Int])),
    "AppId" -> ((b: Builder, v: Any) => b.appId(v.asInstanceOf[String])),
    "ContentEncoding" -> ((b: Builder, v: Any) => b.contentEncoding(v.asInstanceOf[String])),
    "CorrelationId" -> ((b: Builder, v: Any) => b.correlationId(v.asInstanceOf[String])),
    "DeliveryMode" -> ((b: Builder, v: Any) => b.deliveryMode(v.asInstanceOf[Int])),
    "Expiration" -> ((b: Builder, v: Any) => if (v.toString.length > 0) b.timestamp(DatatypeConverter.parseDateTime(v.asInstanceOf[String]).getTime) else b),
    "MessageId" -> ((b: Builder, v: Any) => b.messageId(v.asInstanceOf[String])),
    "ReplyTo" -> ((b: Builder, v: Any) => b.replyTo(v.asInstanceOf[String])),
    "Timestamp" -> ((b: Builder, v: Any) => b.timestamp(DatatypeConverter.parseDateTime(v.asInstanceOf[String]).getTime)),
    "Type" -> ((b: Builder, v: Any) => b.`type`(v.asInstanceOf[String])),
    "ContentType" -> ((b: Builder, v: Any) => b.contentType(v.asInstanceOf[String]))
  )

  def load(body: String) = {
    val mapper = new ObjectMapper()
    val tree = mapper.readTree(body)
    val propsKv = tree.fields()
      .filterNot(kv => propertiesToSkip.contains(kv.getKey))
      .map(kv => (kv.getKey, objectValue(kv.getValue))).toMap
    val headers = tree.get(headersKey).fields().map(kv => (kv.getKey, objectValue(kv.getValue))).toMap
    val props = toBasicProperties(propsKv, headers)
    Message(DatatypeConverter.parseBase64Binary(tree.get(bodyKey).asText()), props)
  }

  def toBasicProperties(kv: Map[String, Any], headers: Map[String, Object]) = {
    val builder = new AMQP.BasicProperties.Builder()
    for ((key, value) <- kv;
         setter <- propertiesSetters.get(key)
    ) {
      setter(builder, value)
    }
    builder.headers(headers.asJava)
    builder.build()
  }

  def objectValue(node: JsonNode): AnyRef = {
    node.getNodeType match {
      case JsonNodeType.STRING => node.asText()
      case JsonNodeType.BOOLEAN => node.asBoolean().asInstanceOf[java.lang.Boolean]
      case JsonNodeType.NUMBER => node.asInt().asInstanceOf[java.lang.Integer]
      case x => throw new Exception("Unknown object type: " + x)
    }
  }
}
