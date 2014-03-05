package net.fawad.rabbitmqloadgen

import java.util.concurrent.ThreadLocalRandom
import com.rabbitmq.client.AMQP.BasicProperties
import javax.xml.parsers.DocumentBuilderFactory
import org.jaxen.dom.DOMXPath
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import org.w3c.dom.{Document, Element}
import java.util
import scala.collection.JavaConversions._
import org.w3c.dom.bootstrap.DOMImplementationRegistry
import org.w3c.dom.ls.DOMImplementationLS
import scala.concurrent.{ExecutionContext, Future}

object MessageTransformers {
  val random = ThreadLocalRandom.current()
  val dbFactory = DocumentBuilderFactory.newInstance()
  implicit val ctx = ExecutionContext.Implicits.global

  def XpathRandomBodyReplace(xpathStrings: Iterable[String]) = {
    val xpaths = xpathStrings.map(new DOMXPath(_))
    (msg: Message) => {
      val doc = parseXML(msg)
      for (xpath <- xpaths) {
        val results = xpath.evaluate(doc).asInstanceOf[util.ArrayList[Element]]
        for (node <- results) {
          val newValue = Math.abs(random.nextInt()).toString
          node.setTextContent(newValue)
        }
      }
      Message(serializeDocument(doc).toByteArray, updateIds(msg.properties))
    }
  }

  def XpathConstantBodyReplace(xpathReplacements: Map[String, String]) = {
    val xpaths = xpathReplacements.map {
      case (k, v) => (new DOMXPath(k), v)
    }
    (msg: Message) => {
      val doc = parseXML(msg)
      for ((xpath, replacement) <- xpaths) {
        val results = xpath.evaluate(doc).asInstanceOf[util.ArrayList[Element]]
        for (node <- results) {
          node.setTextContent(replacement)
        }
      }
      Message(serializeDocument(doc).toByteArray, updateIds(msg.properties))
    }
  }


  def serializeDocument(doc: Document) = {
    val baos = new ByteArrayOutputStream()
    val reg = DOMImplementationRegistry.newInstance()
    val impl = reg.getDOMImplementation("LS").asInstanceOf[DOMImplementationLS]
    val serializer = impl.createLSSerializer()
    val lso = impl.createLSOutput()
    lso.setByteStream(baos)
    serializer.write(doc, lso)
    baos
  }

  def parseXML(msg: Message): Document = {
    val dBuilder = dbFactory.newDocumentBuilder()
    val doc = dBuilder.parse(new ByteArrayInputStream(msg.body))
    doc
  }

  def updateIds(props: BasicProperties) = props.builder()
    .messageId(random.nextLong().toString)
    .correlationId(random.nextLong().toString)
    .build()
}
