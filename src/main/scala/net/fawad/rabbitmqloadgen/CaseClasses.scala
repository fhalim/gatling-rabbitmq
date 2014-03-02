package net.fawad.rabbitmqloadgen

case class Message(body: Array[Byte], headers: Map[String, String])

case class Publish(msg: Message, exchangeInfo: ExchangeInfo)

case class ConnectionInfo(hostname: String, port: Int, userName: String, password: String)

case class ExchangeInfo(name: String, exchangeType: String)

case class InitializeSubscriber(exchangeInfo: ExchangeInfo)

case class Success()