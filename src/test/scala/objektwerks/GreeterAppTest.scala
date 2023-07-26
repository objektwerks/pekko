package objektwerks

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike

import objektwerks.Greeter.Greet
import objektwerks.Greeter.Greeted

class GreeterAppTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike:
  test("greeting") {
    val replyProbe = createTestProbe[Greeted]()
    val underTest = spawn(Greeter())
    underTest ! Greet("Santa", replyProbe.ref)
    replyProbe.expectMessage(Greeted("Santa", underTest.ref))
  }