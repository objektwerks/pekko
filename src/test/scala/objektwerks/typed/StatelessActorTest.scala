package objektwerks.typed

import org.apache.pekko.actor.typed.{Behavior, PostStop}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit

import org.scalatest.wordspec.AnyWordSpecLike

sealed trait Count
case object Increment extends Count
case object Decrement extends Count

object CountActor:
  def behavior(count: Int = 0): Behavior[Count] = Behaviors.receive[Count] {
    (context, message) => message match {
      case Increment =>
        context.log.info("*** Increment +1, Count: ", count + 1)
        behavior(count + 1)
      case Decrement =>
        context.log.info("*** Decrement -1, Count: ", count - 1)
        behavior(count - 1)
    }
  }.receiveSignal {
    case (context, PostStop) =>
      context.log.info("*** CountActor stopped!")
      Behaviors.same
  }

class StatelessActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike:
  "CountActor behavior" should {
    "increment / decrement" in {
      val testProbe = createTestProbe[Count]("test-count")
      val countActor = spawn(CountActor.behavior(), "count-actor")
      countActor ! Increment
      testProbe.expectNoMessage()
      countActor ! Decrement
      testProbe.expectNoMessage()
    }
  }