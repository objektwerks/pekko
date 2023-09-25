package objektwerks.actor

import java.time.LocalTime

import org.apache.pekko.actor._
import org.apache.pekko.pattern._
import org.apache.pekko.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import org.apache.pekko.util.Timeout

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

final class Clock extends Actor with ActorLogging:
  val router = {
    val routees = Vector.fill(2) {
      ActorRefRoutee( context.actorOf(Props[Time]()) )
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive: Receive =
    case timeIs: String => router.route(timeIs, sender())

final class Time extends Actor with ActorLogging:
  def receive: Receive =
    case timeIs: String =>
      val time = s"$timeIs ${LocalTime.now.toString}"
      log.info(s"*** $time")
      sender().tell(time, context.parent)

final class RouterTest extends AnyFunSuite with BeforeAndAfterAll:
  given timeout: Timeout = Timeout(1 second)
  val system = ActorSystem.create("router", Conf.config)
  val clock = system.actorOf(Props[Clock](), name = "clock")

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 1 second)
    ()

  test("router"):
    val whatTimeIsIt = (i: Int) => {
      val future = ask(clock, s"$i. The time is:").mapTo[String]
      val time = Await.result(future, 1 second)
      assert(time.nonEmpty)
    }
    for(i <- 1 to 3) whatTimeIsIt(i)
