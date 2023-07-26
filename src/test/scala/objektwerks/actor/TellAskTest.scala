package akka

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.pattern._
import akka.util.Timeout

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

sealed trait Message
case class Tell(message: String) extends Message
case class TellWorker(message: String) extends Message
case class Ask(message: String) extends Message
case class AskWorker(message: String) extends Message

class Master extends Actor with ActorLogging {
  import context.dispatcher

  implicit val timeout = new Timeout(1, TimeUnit.SECONDS)
  val worker = context.actorOf(Props[Worker](), name = "worker")

  def receive: Receive = {
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
  }
}

class Worker extends Actor with ActorLogging {
  def receive: Receive = {
    case TellWorker(message) =>
      log.info(s"*** [Tell Worker] Worker received tell worker message from master: $message.")
    case AskWorker(message) =>
      log.info(s"*** [Ask Worker] Worker received ask worker message from master: $message.")
      sender() ! s"Worker responded to $message."
  }
}

class TellAskTest extends AnyFunSuite with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)
  val system = ActorSystem.create("tellask", Conf.config)
  val master = system.actorOf(Props[Master](), name = "master")

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 1 second)
    ()
  }

  test("master ! tell") {
    master ! Tell("master ! tell")
  }

  test("master ! tell worker") {
    master ! TellWorker("master ! tell worker")
  }

  test("master ? ask") {
    assert( Await.result((master ? Ask("master ? ask")).mapTo[String], 1 second).nonEmpty )
  }

  test("master ? ask worker") {
    assert( Await.result((master ? AskWorker("master ? ask worker")).mapTo[String], 1 second).nonEmpty )
  }
}