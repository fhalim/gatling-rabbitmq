package net.fawad.rabbitmqloadgen

import resource._
import com.rabbitmq.client.Connection


case class ConnectionInfo(hostname:String, port:Int, userName: String, password: String)
case class ExchangeInfo(name: String, exchangeType: String)
class RabbitMQPublisher(conn:Connection, exchangeInfo:ExchangeInfo) {
  def init(){
    for(channel <- managed(conn.createChannel())){
      channel.exchangeDeclare(exchangeInfo.name, exchangeInfo.exchangeType, true)
    }
  }
  def publishMessage(){

  }
}
