package objektwerks.actor

import org.apache.pekko.actor.*
import org.apache.pekko.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

class Echo extends Actor {
  def receive: Receive = {
    case echo: String => sender() ! echo
  }
}

class ActorTest extends TestKit(ActorSystem("actor-test", Conf.config))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {
  val echo = system.actorOf(Props[Echo](), name = "echo")

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "Echo actor" should {
    "expect ping" in {
      within(1 second) {
        echo ! "ping"
        expectMsg("ping")
      }
    }

    "expect pong via test probe" in {
      val probe = TestProbe("echo-test-probe")
      probe.send(echo, "pong")
      probe.expectMsg(1 second, "pong")
    }

    "expect test via test actor ref" in {
      val testEchoRef = TestActorRef[Echo](Props[Echo](), name = "echo-test-actor-ref")
      testEchoRef ! "test"
      expectMsg("test")
    }
  }
}