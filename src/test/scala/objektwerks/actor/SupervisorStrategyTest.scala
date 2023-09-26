package objektwerks.actor

import org.apache.pekko.actor.SupervisorStrategy.Restart
import org.apache.pekko.actor.*
import org.apache.pekko.util.Timeout

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps

sealed trait Task
case object Play extends Task
case object CleanRoom extends Task

final class CleanRoomException(cause: String) extends Exception(cause)

final class Nanny extends Actor with ActorLogging:
  given Timeout = Timeout(3 seconds)
  val child = context.actorOf(Props[Child](), name = "child")

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1 second) {
      case _: CleanRoomException => Restart
    }

  def receive: Receive =
    case task: Task => child ! task

final class Child extends Actor with ActorLogging:
  def receive: Receive =
    case Play => log.info("*** Child happily playing!")
    case CleanRoom => throw new CleanRoomException("Child refuses to clean room!")

final class SupervisorStrategyTest extends AnyFunSuite with BeforeAndAfterAll:
  given Timeout = Timeout(1 second)
  val system = ActorSystem.create("supervisor", Conf.config)
  val nanny = system.actorOf(Props[Nanny](), name = "nanny")

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 1 second)
    ()

  test("nanny ! child"):
    nanny ! Play
    nanny ! CleanRoom
    Thread.sleep(3000)
    nanny ! Play