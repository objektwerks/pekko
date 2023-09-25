package objektwerks.actor

import org.apache.pekko.actor.*
import org.apache.pekko.pattern.*
import org.apache.pekko.util.Timeout

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps

sealed trait Message
final case class Tell(message: String) extends Message
final case class TellWorker(message: String) extends Message
final case class Ask(message: String) extends Message
final case class AskWorker(message: String) extends Message

final class Master extends Actor with ActorLogging:
  import context.dispatcher

  given timeout: Timeout = new Timeout(1 second)
  val worker = context.actorOf(Props[Worker](), name = "worker")

  def receive: Receive =
    case Tell(message) =>
      log.info(s"*** [Tell] Master received tell message: $message.")
    case tellWorker @ TellWorker(message) =>
      log.info(s"*** [Tell Worker] Master received tell worker message: $message.")
      worker ! tellWorker
    case Ask(message) =>
      log.info(s"*** [Ask] Master received and responded to ask message: $message.")
      sender() ! s"*** Master responded to ask $message."
    case askWorker @ AskWorker(message) =>
      log.info(s"*** [Ask Worker] Master received ask worker message: $message.")
      log.info(s"*** [Ask Worker] ask worker message ? Worker, pipeTo Master: $message.")
      worker ? askWorker pipeTo sender()
      ()

final class Worker extends Actor with ActorLogging:
  def receive: Receive =
    case TellWorker(message) =>
      log.info(s"*** [Tell Worker] Worker received tell worker message from master: $message.")
    case AskWorker(message) =>
      log.info(s"*** [Ask Worker] Worker received ask worker message from master: $message.")
      sender() ! s"Worker responded to $message."

final class TellAskTest extends AnyFunSuite with BeforeAndAfterAll:
  given timeout: Timeout = Timeout(1 second)
  val system = ActorSystem.create("tellask", Conf.config)
  val master = system.actorOf(Props[Master](), name = "master")

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 1 second)
    ()

  test("master ! tell"):
    master ! Tell("master ! tell")

  test("master ! tell worker"):
    master ! TellWorker("master ! tell worker")

  test("master ? ask"):
    assert( Await.result((master ? Ask("master ? ask")).mapTo[String], 1 second).nonEmpty )

  test("master ? ask worker"):
    assert( Await.result((master ? AskWorker("master ? ask worker")).mapTo[String], 1 second).nonEmpty )
