package net.fawad.rabbitmqloadgen.tests

import org.scalatest._
import net.fawad.rabbitmqloadgen.{MessageTransformers, RabbitMQMessageReader}
import scala.io.Source
import resource.managed
import scala.concurrent.duration._
import akka.util.Timeout

class MessageTransformersTests extends FlatSpec {
  "The Message Transformer" should "be able to randomly process existing requests" in {
    val reader = new RabbitMQMessageReader()
    for (stream <- managed(this.getClass.getClassLoader.getResourceAsStream("sample_signing_request.json"))) {
      val contents = Source.fromInputStream(stream).mkString
      val result = MessageTransformers.XpathRandomBodyReplace(List("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='dealJacketId']"))(reader.load(contents))
      assert(!new String(result.body).contains("NTk3ODNiYjItOTY1Mi00ODljLWEwYjgtZmZiZDk4MWM3NWUz"))
    }
  }

  "The Message Transformer" should "be able to substitute existing requests" in {
    val reader = new RabbitMQMessageReader()
    for (stream <- managed(this.getClass.getClassLoader.getResourceAsStream("sample_signing_request.json"))) {
      val contents = Source.fromInputStream(stream).mkString
      val result = MessageTransformers.XpathConstantBodyReplace(Map("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='departmentId']" -> "1234"))(reader.load(contents))
      assert(new String(result.body).contains("<departmentId>1234</departmentId>"))
    }

  }

  "The Message Transformer" should "perform reasonably" in {
    val reader = new RabbitMQMessageReader()
    for (stream <- managed(this.getClass.getClassLoader.getResourceAsStream("sample_signing_request.json"))) {
      val contents = Source.fromInputStream(stream).mkString
      val iterations = 1000
      val startTime = System.currentTimeMillis()
      val transformer = MessageTransformers.XpathRandomBodyReplace(List("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='dealJacketId']"))
      for (x <- 1 until iterations) {
        val result = transformer(reader.load(contents))
        assert(!new String(result.body).contains("NTk3ODNiYjItOTY1Mi00ODljLWEwYjgtZmZiZDk4MWM3NWUz"))
      }
      val totalTime = System.currentTimeMillis() - startTime
      val timePerIteration = 1.0 * totalTime / iterations
      println(timePerIteration)
      assert(timePerIteration < 10)
    }
  }
}
