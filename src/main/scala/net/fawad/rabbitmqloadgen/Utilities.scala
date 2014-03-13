package net.fawad.rabbitmqloadgen

import java.io.{FileInputStream, File}
import java.util.Properties
import resource.managed

object Utilities {
  def time[T](action: => T) = {
    val start = System.currentTimeMillis()
    action
    System.currentTimeMillis() - start
  }

  def properties(fileName: File) =
    managed(new FileInputStream(fileName)) map {
      file =>
        val props = new Properties()
        props.load(file)
        props
    }

  def connectivityInformationFromPropertiesFile(fileName: File) = {
    val props = properties(fileName)
    props.map {
      p => {
        val hostName = p.getProperty("hostname", "localhost")
        val username = p.getProperty("username", "guest")
        val password = p.getProperty("password", "guest")
        val port = Integer.parseInt(p.getProperty("port", "5672"))
        val vhost = p.getProperty("vhost", "/")
        val exchangeName = p.getProperty("exchange.name", "fawad.benchmark")
        val exchangeType = p.getProperty("exchange.type", "fanout")
        RabbitMQConnectivityInformation(ConnectionInfo(hostName, port, username, password, vhost), ExchangeInfo(exchangeName, exchangeType))
      }
    }
  }
}
