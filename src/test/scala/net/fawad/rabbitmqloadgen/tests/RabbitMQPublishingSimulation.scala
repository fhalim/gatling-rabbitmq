package net.fawad.rabbitmqloadgen.tests

import io.gatling.core.Predef._
import scala.concurrent.duration._
import akka.actor.ActorRef
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.system
import bootstrap._
import assertions._
import akka.actor.Props
import net.fawad.rabbitmqloadgen.PublishToRabbitMQAction


class RabbitMQPublishingSimulation extends Simulation {

  val mine = new ActionBuilder {
    def build(next: ActorRef) = system.actorOf(Props(new PublishToRabbitMQAction(next)))
  }
  val scn = scenario("RabbitMQ Publishing")
    .repeat(2) {
    exec(mine)
  }

  setUp(scn.inject(ramp(5 users) over (10 seconds)))
    //Assert the output max time is less than 50ms and 95% of requests were successful
    .assertions(global.responseTime.max.lessThan(50),global.successfulRequests.percent.greaterThan(95))
}


