package net.fawad.rabbitmqloadgen

import com.rabbitmq.client.Connection
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory
import com.rabbitmq.client.AMQP.BasicProperties


case class ConnectionInfo(hostname: String, port: Int, userName: String, password: String)

case class ExchangeInfo(name: String, exchangeType: String)

class RabbitMQPublisher(conn: Connection, exchangeInfo: ExchangeInfo) {
  protected lazy val logger: Logger =
    Logger(LoggerFactory getLogger getClass.getName)

  def initialize() {
    for(channel <- conn.createChannel()){
      logger.info("Initializing RabbitMQ exchange %s" format exchangeInfo.name)
      channel.exchangeDeclare(exchangeInfo.name, exchangeInfo.exchangeType, true)
    }

  }

  def publishMessage() {
    for(channel <- conn.createChannel()){
      logger.debug("Publishing message")
      channel.basicPublish(exchangeInfo.name, "", new BasicProperties(), "Hello, world".getBytes)
    }
  }
}
