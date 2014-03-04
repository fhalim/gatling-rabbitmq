package net.fawad.rabbitmqloadgen.tests

import org.scalatest._
import net.fawad.rabbitmqloadgen.RabbitMQMessageReader
import java.io.File
import scala.io.Source
import scala.collection.JavaConversions._

class RabbitMQMessageReaderTests extends FlatSpec {
  "The Message Reader" should "be able to read a simple message" in {
    val result = new RabbitMQMessageReader().load( """{"Body":"ZmF3YWQ=", "Headers": {"a":"B"}, "ContentType": "bar"}""")
    assert(result.body === "fawad".getBytes)
    assert(result.properties.getContentType == "bar")
    assert(result.properties.getHeaders()("a") === "B")
  }

  "The Message Reader" should "be able to process all existing messages" in {
    val reader = new RabbitMQMessageReader()
    for (fileName <- new File("/Users/halimf/tmp/messagedumps").listFiles().filter(_.isFile)) {
      val contents = Source.fromFile(fileName).mkString
      val result = reader.load(contents)
      assert(result.body != null)
      assert(result.properties != null)
      assert(result.properties.getHeaders != null)
      assert(result.properties.getType != null)
    }
  }
}
