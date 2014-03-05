package net.fawad.rabbitmqloadgen.tests

import org.scalatest._
import net.fawad.rabbitmqloadgen.RabbitMQMessageReader
import scala.io.Source
import scala.collection.JavaConversions._
import resource.managed

class RabbitMQMessageReaderTests extends FlatSpec {
  "The Message Reader" should "be able to read a simple message" in {
    val result = new RabbitMQMessageReader().load( """{"Body":"ZmF3YWQ=", "Headers": {"a":"B"}, "ContentType": "bar"}""")
    assert(result.body === "fawad".getBytes)
    assert(result.properties.getContentType == "bar")
    assert(result.properties.getHeaders()("a") === "B")
  }

  "The Message Reader" should "be able to process existing requests" in {
    val reader = new RabbitMQMessageReader()
    for (stream <- managed(this.getClass.getClassLoader.getResourceAsStream("sample_signing_request.json"))) {
      val contents = Source.fromInputStream(stream).mkString
      val result = reader.load(contents)
      assert(result.body != null)
      assert(result.properties != null)
      assert(result.properties.getHeaders != null)
      assert(result.properties.getType != null)
    }
  }
}
