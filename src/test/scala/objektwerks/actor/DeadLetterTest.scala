package objektwerks.actor

import org.apache.pekko.actor.*
import org.apache.pekko.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps

final class Service extends Actor with ActorLogging:
  def receive: Receive =
    case message: String => log.info(s"*** Service received: $message")

final class Listener extends Actor with ActorLogging:
  def receive: Receive =
    case _: DeadLetter => log.info(s"*** Listener received dead letter!")

final class DeadLetterTest extends AnyFunSuite with BeforeAndAfterAll:
  given Timeout = Timeout(1 second)
  val system = ActorSystem.create("deadletter", Conf.config)
  val service = system.actorOf(Props[Service](), name = "service")
  val listener = system.actorOf(Props[Listener](), name = "listener")
  system.eventStream.subscribe(listener, classOf[DeadLetter])

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 1 second)
    ()

  test("dead letter"):
    service ! "First message!"
    Thread.sleep(1000)
    service ! PoisonPill
    Thread.sleep(1000)
    service ! "Second message!"