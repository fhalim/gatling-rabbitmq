package net.fawad.rabbitmqloadgen.tests

import io.gatling.core.Predef._
import scala.concurrent.duration._
import akka.actor.ActorRef
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.system
import bootstrap._
import assertions._
import akka.actor.Props
import net.fawad.rabbitmqloadgen.{RabbitMQPublisher, ExchangeInfo, ConnectionInfo, PublishToRabbitMQAction}


class RabbitMQPublishingSimulation extends Simulation {
  val connectionInfo = ConnectionInfo("localhost", 5672, "guest", "guest")
  val exchangeInfo = ExchangeInfo("fawad.benchmark", "fanout")
  val conn = PublishToRabbitMQAction.createConnection(connectionInfo)

  // TODO: This is probably a stink. Figure out a good way of handling this
  new RabbitMQPublisher(conn, ExchangeInfo(exchangeInfo.name, exchangeInfo.exchangeType)).initialize()

  val publishToRabbitMQ = new ActionBuilder {
    def build(next: ActorRef) = system.actorOf(Props(new PublishToRabbitMQAction(next, conn, exchangeInfo)))
  }
  val scn = scenario("RabbitMQ Publishing")
    .repeat(10000) {
    exec(publishToRabbitMQ)
  }

  setUp(scn.inject(ramp(5 users) over (10 seconds)))
    //Assert the output max time is less than 50ms and 95% of requests were successful
    .assertions(global.responseTime.max.lessThan(50), global.successfulRequests.percent.greaterThan(95))
}


