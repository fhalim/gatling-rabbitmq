package net.fawad.rabbitmqloadgen

import akka.actor.ActorRef
import scala.Some
import io.gatling.core.result.message.{Status, RequestMessage, KO, OK}
import io.gatling.core.result.writer.DataWriter
import com.rabbitmq.client.{Connection, ConnectionFactory}
import io.gatling.core.action.Chainable
import io.gatling.core.session.Session

object PublishToRabbitMQAction {
  def createConnection(connectionInfo: ConnectionInfo) = {
    val factory = new ConnectionFactory()
    factory.setHost(connectionInfo.hostname)
    factory.setPort(connectionInfo.port)
    val conn = factory.newConnection()
    conn
  }
}
class PublishToRabbitMQAction(val next: ActorRef, conn: Connection, exchangeInfo: ExchangeInfo) extends Chainable {
  val pub = new RabbitMQPublisher(conn, ExchangeInfo(exchangeInfo.name, exchangeInfo.exchangeType))

  override def execute(session: Session) {
    var start: Long = 0L
    var end: Long = 0L
    var status: Status = OK
    var errorMessage: Option[String] = None
    try {
      start = System.currentTimeMillis
      pub.publishMessage()
      end = System.currentTimeMillis
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
