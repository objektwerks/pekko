package objektwerks.typed

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit

import org.scalatest.funsuite.AnyFunSuiteLike

import objektwerks.typed.Greeter.{Greet, Greeted}

final class GreeterAppTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike:
  test("greeter"):
    val replyProbe = createTestProbe[Greeted]()
    val underTest = spawn(Greeter())
    underTest ! Greet("Santa", replyProbe.ref)
    replyProbe.expectMessage(Greeted("Santa", underTest.ref))
