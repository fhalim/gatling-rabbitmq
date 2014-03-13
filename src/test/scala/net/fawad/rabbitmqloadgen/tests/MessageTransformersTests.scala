package net.fawad.rabbitmqloadgen.tests

import org.scalatest._
import net.fawad.rabbitmqloadgen.{Utilities, MessageTransformers, RabbitMQMessageReader}
import scala.io.Source
import resource.managed
import MessageTransformers._

class MessageTransformersTests extends FlatSpec {
  "The Message Transformer" should "be able to randomly process existing requests" in {
    val reader = new RabbitMQMessageReader()
    for (stream <- managed(this.getClass.getClassLoader.getResourceAsStream("sample_signing_request.json"))) {
      val contents = Source.fromInputStream(stream).mkString
      val msg = reader.load(contents)
      val doc = MessageTransformers.parseXML(msg)
      val result = xml(List(xpathRandom(List("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='dealJacketId']"))))(msg)
      assert(!new String(result.body).contains("NTk3ODNiYjItOTY1Mi00ODljLWEwYjgtZmZiZDk4MWM3NWUz"))
    }
  }

  it should "be able to substitute existing requests" in {
    val reader = new RabbitMQMessageReader()
    for (stream <- managed(this.getClass.getClassLoader.getResourceAsStream("sample_signing_request.json"))) {
      val contents = Source.fromInputStream(stream).mkString
      val msg = reader.load(contents)
      val doc = MessageTransformers.parseXML(msg)
      val result = xml(List(xpathConstant(Map("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='departmentId']" -> "1234"))))(msg)
      assert(new String(result.body).contains("<departmentId>1234</departmentId>"))
    }

  }

  it should "perform reasonably" in {
    import Utilities.time
    val reader = new RabbitMQMessageReader()
    for (stream <- managed(this.getClass.getClassLoader.getResourceAsStream("sample_signing_request.json"))) {
      val contents = Source.fromInputStream(stream).mkString
      val iterations = 1000
      val msg = reader.load(contents)
      val totalTime = time {
        val transformer = xml(List(xpathRandom(List("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='dealJacketId']"))))
        for (x <- 1 until iterations) {
          val result = transformer(msg)
          assert(!new String(result.body).contains("NTk3ODNiYjItOTY1Mi00ODljLWEwYjgtZmZiZDk4MWM3NWUz"))
        }
      }
      val timePerIteration = 1.0 * totalTime / iterations
      println(s"Average time per iteration $timePerIteration")
      assert(timePerIteration < 10)
    }
  }
}
