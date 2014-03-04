package net.fawad.rabbitmqloadgen

import com.rabbitmq.client.{AMQP, Channel, ConnectionFactory}
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory
import akka.actor.Actor
import resource.managed

class RabbitMQInteractor(connectionInfo: ConnectionInfo) extends Actor {
  val conn = createConnection(connectionInfo)
  protected lazy val logger: Logger =
    Logger(LoggerFactory getLogger getClass.getName)

  override def receive = {
    case Publish(msg, exchangeInfo) => for (channel <- managed(conn.createChannel())) {
      logger.debug("Publishing message")
      channel.basicPublish(exchangeInfo.name, "", msg.properties, msg.body)
      sender ! Success()
    }
    case InitializeSubscriber(exchangeInfo) =>
      onChannel {
        channel =>
          logger.info("Initializing RabbitMQ exchange %s" format exchangeInfo.name)
          channel.exchangeDeclare(exchangeInfo.name, exchangeInfo.exchangeType, true)
      }
      onChannel {
        channel =>
          logger.info("Initializing RabbitMQ queue %s" format exchangeInfo.name)
          channel.queueDeclare(exchangeInfo.name, true, false, false, null)
      }
      onChannel {
        channel =>
          logger.info("Initializing RabbitMQ binding %s" format exchangeInfo.name)
          channel.queueBind(exchangeInfo.name, exchangeInfo.name, "")
      }
      sender ! Success()
  }

  def onChannel[A](action: Channel => A) = {
    for (channel <- managed(conn.createChannel())) {
      action(channel)
    }
  }

  def createConnection(connectionInfo: ConnectionInfo) = {
    val factory = new ConnectionFactory()
    factory.setHost(connectionInfo.hostname)
    factory.setPort(connectionInfo.port)
    factory.setUsername(connectionInfo.userName)
    factory.setPassword(connectionInfo.password)
    factory.setVirtualHost("/")
    factory.newConnection()
  }
}
