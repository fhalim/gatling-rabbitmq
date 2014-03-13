package net.fawad.rabbitmqloadgen

import com.rabbitmq.client.AMQP.BasicProperties

case class Message(body: Array[Byte], properties: BasicProperties)

case class Publish(msg: Message, exchangeInfo: ExchangeInfo)

case class ConnectionInfo(hostname: String, port: Int, userName: String, password: String, vhost:String = "/")

case class ExchangeInfo(name: String, exchangeType: String)

case class InitializeSubscriber(exchangeInfo: ExchangeInfo)

case class RabbitMQConnectivityInformation(connectionInfo:ConnectionInfo, exchangeInfo:ExchangeInfo)