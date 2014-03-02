package net.fawad.rabbitmqloadgen.tests

import io.gatling.core.Predef._
import scala.concurrent.duration._
import akka.actor.ActorRef
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.system
import bootstrap._
import assertions.global
import akka.actor.Props
import akka.pattern.ask
import net.fawad.rabbitmqloadgen._
import akka.routing.RoundRobinRouter
import net.fawad.rabbitmqloadgen.ExchangeInfo
import net.fawad.rabbitmqloadgen.ConnectionInfo
import akka.util.Timeout
import scala.concurrent.Await

class RabbitMQPublishingSimulation extends Simulation {
  implicit val timeout = Timeout(5 seconds)
  val exchangeInfo = ExchangeInfo("fawad.benchmark", "fanout")
  val interactors = system.actorOf(Props(new RabbitMQInteractor(ConnectionInfo("localhost", 5672, "guest", "guest"))).withRouter(RoundRobinRouter(nrOfInstances = 5)))
  // TODO: This is probably a stink. Figure out a good way of handling this
  Await.result(interactors ask InitializeSubscriber(exchangeInfo), Duration.Inf)
  val publishToRabbitMQ = new ActionBuilder {
    def build(next: ActorRef) = system.actorOf(Props(new PublishToRabbitMQAction(next, interactors, exchangeInfo)))
  }
  val scn = scenario("RabbitMQ Publishing")
    .repeat(100000) {
    exec(publishToRabbitMQ)
  }

  setUp(scn.inject(ramp(5 users) over (2 seconds)))
    .assertions(global.responseTime.max.lessThan(20), global.successfulRequests.percent.is(100))
}


