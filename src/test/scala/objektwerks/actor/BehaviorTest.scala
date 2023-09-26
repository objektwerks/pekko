package objektwerks.actor

import org.apache.pekko.actor.*
import org.apache.pekko.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps

case object Ready
case object Swim
case object Bike
case object Run
case object Finish

final class Triathlete extends Actor with ActorLogging:
  def receive: Receive = prepare

  def prepare: Receive =
    case Ready => log.info("*** Triathlete ready!")
    case Swim => log.info("*** Triathlete swimming!"); context.become(swim)

  def swim: Receive =
    case Bike => log.info("*** Triathlete biking!"); context.become(bike)

  def bike: Receive =
    case Run => log.info("*** Triathlete running!"); context.become(run)

  def run: Receive =
    case Finish => log.info("*** Triathlete finished race!"); context.become(prepare)

  override def unhandled(message: Any): Unit =
    super.unhandled(message)
    log.info(s"*** Triathlete failed to handle message: $message.")

final class BehaviorTest extends AnyFunSuite with BeforeAndAfterAll:
  given Timeout = Timeout(1 second)
  val system = ActorSystem.create("behavior", Conf.config)
  val triathlete = system.actorOf(Props[Triathlete](), name = "triathlete")

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 1 second)
    ()

  test("race"):
    triathlete ! Ready
    triathlete ! Swim
    triathlete ! Bike
    triathlete ! Run
    triathlete ! Finish