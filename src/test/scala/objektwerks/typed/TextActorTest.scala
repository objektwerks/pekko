package objektwerks.typed

import org.apache.pekko.actor.typed.{ActorRef, Behavior, PostStop}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit

import org.scalatest.wordspec.AnyWordSpecLike

final case class Text(text: String, replyTo: ActorRef[Echo])
final case class Echo(text: String)

object TextActor:
  def apply(): Behavior[Text] = Behaviors.receive[Text] {
    (context, text) => text match {
      case Text(text, replyTo) =>
        context.log.info("*** Text = {} from {}", text, replyTo.path.name)
        replyTo ! Echo(text)
        Behaviors.same
    }
  }.receiveSignal {
    case (context, PostStop) =>
      context.log.info("*** TextActor stopped!")
      Behaviors.same
  }
}

class TextActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike:
  "TextActor behavior" should {
    "text / echo" in {
      val testProbe = createTestProbe[Echo]("test-probe")
      val textActor = spawn(TextActor(), "text-actor")
      textActor ! Text("abc123", testProbe.ref)
      testProbe.expectMessage(Echo("abc123"))
    }