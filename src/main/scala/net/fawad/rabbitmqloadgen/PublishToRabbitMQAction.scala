package net.fawad.rabbitmqloadgen

import akka.actor.ActorRef
import scala.Some
import io.gatling.core.result.message.{Status, RequestMessage, KO, OK}
import io.gatling.core.result.writer.DataWriter
import com.rabbitmq.client.ConnectionFactory
import io.gatling.core.action.Chainable
import io.gatling.core.session.Session

class PublishToRabbitMQAction(val next: ActorRef) extends Chainable {
  val factory = new ConnectionFactory()
  factory.setHost("localhost")
  factory.setPort(5672)
  logger.info("Creating connection")
  val conn = factory.newConnection()
  val pub = new RabbitMQPublisher(conn, ExchangeInfo("demoexchange", "fanout"))
  pub.init()

  def doStuff() {
    pub.publishMessage()
  }

  override def execute(session: Session) {
    var start: Long = 0L
    var end: Long = 0L
    var status: Status = OK
    var errorMessage: Option[String] = None
    try {
      start = System.currentTimeMillis;
      doStuff()
      end = System.currentTimeMillis;
    } catch {
      case e: Exception =>
        errorMessage = Some(e.getMessage)
        logger.error("Unable to publish", e)
        status = KO
    } finally {
      DataWriter.tell(
        RequestMessage(session.scenarioName, session.userId, session.groupStack, "RabbitMQ Publishing",
          start, start, end, end,
          status, errorMessage, Nil))
      next ! session
    }
  }
}
