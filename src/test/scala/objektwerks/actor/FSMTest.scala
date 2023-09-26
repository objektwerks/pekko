package objektwerks.actor

import org.apache.pekko.actor.*
import org.apache.pekko.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps

sealed trait State
case object Off extends State
case object On extends State

final case class Data(flowRate: Int)

final case class Event(state: State, data: Data)

final class Pump extends Actor with FSM[State, Data]:
  startWith(Off, Data(0))
  when(Off):
    case Event(On, Data(flowRate)) => goto(On) using Data(flowRate)
  when(On):
    case Event(Off, Data(flowRate)) => goto(Off) using Data(flowRate)
  initialize()

final class FSMTest extends AnyFunSuite with BeforeAndAfterAll:
  given Timeout = Timeout(1 second)
  val system = ActorSystem.create("fsm", Conf.config)
  val pump = system.actorOf(Props[Pump](), name = "pump")

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 1 second)
    ()

  test("fsm"):
    pump ! Event(On, Data(1))
    pump ! Event(Off, Data(0))