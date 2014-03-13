package net.fawad.rabbitmqloadgen

import akka.actor.ActorRef
import scala.Some
import io.gatling.core.result.message.{Status, RequestMessage, KO, OK}
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.action.Chainable
import io.gatling.core.session.Session
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure
import akka.util.Timeout

class PublishToRabbitMQAction(val next: ActorRef, interactor: ActorRef, exchangeInfo: ExchangeInfo, gen: Iterator[Message]) extends Chainable {
  override def execute(session: Session) {
    var start: Long = 0L
    var end: Long = 0L
    val timeout = Timeout(60 seconds)
    var status: Status = OK
    var errorMessage: Option[String] = None
    try {
      start = System.currentTimeMillis
      val msg = gen.next()
      Await.result((interactor ask Publish(msg, exchangeInfo))(timeout), Duration.Inf) match {
        case Failure(e) => throw e
        case _ =>
      }
    } catch {
      case e: Exception =>
        errorMessage = Some(e.getMessage)
        logger.error("Unable to publish", e)
        status = KO
    } finally {
      end = System.currentTimeMillis
      DataWriter.tell(
        RequestMessage(session.scenarioName, session.userId, session.groupStack, "RabbitMQ Publishing",
          start, start, end, end,
          status, errorMessage, Nil))
      next ! session
    }
  }
}
