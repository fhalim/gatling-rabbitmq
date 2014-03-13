package net.fawad.rabbitmqloadgen.simulations

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
import akka.util.Timeout
import scala.concurrent.Await
import java.io.File
import MessageTransformers._

class RabbitMQPublishingSimulation extends Simulation {
  implicit val timeout = Timeout(60 seconds)
  val parallelism = 10
  

  val connectivityInfo = Utilities.connectivityInformationFromPropertiesFile(new File("config.properties"))
  val interactors = system.actorOf(Props(new RabbitMQInteractor(connectivityInfo.opt.get.connectionInfo)).withRouter(RoundRobinRouter(nrOfInstances = 10)))

  // TODO: This is probably a stink. Figure out a good way of handling this
  Await.result(interactors ask InitializeSubscriber(connectivityInfo.opt.get.exchangeInfo), Duration.Inf)

  val transformer = xml(List(xpathRandom(List("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='dealJacketId' or local-name()='dealNumber']")),
    xpathConstant(Map("/*[local-name()='SigningRequest']/*[local-name()='source']/*[local-name()='departmentId']" -> "abcd"))))
  val gen = new MessageGenerator(new File("requests")).iterator.map(transformer)

  val publishToRabbitMQ = new ActionBuilder {
    def build(next: ActorRef) = system.actorOf(Props(new PublishToRabbitMQAction(next, interactors, connectivityInfo.opt.get.exchangeInfo, gen)))
  }

  def setGenerator(session: Session) {
    session.set("MessageToPublish", gen)
  }

  val scn = scenario("RabbitMQ Publishing")
    .repeat(100) {
    exec(publishToRabbitMQ)
  }

  setUp(scn.inject(ramp(parallelism users) over (2 seconds)))
    .assertions(global.responseTime.max.lessThan(20), global.successfulRequests.percent.is(100))
}


