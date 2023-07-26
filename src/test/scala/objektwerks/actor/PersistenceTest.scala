package objektwerks.actor

import java.time.LocalTime
import java.util.UUID

import org.apache.pekko.actor.{ActorLogging, ActorSystem, Props}
import org.apache.pekko.pattern.*
import org.apache.pekko.persistence.*
import org.apache.pekko.util.Timeout

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.concurrent.ExecutionContext

case class Compute(f: Int => Int,
                   n: Int,
                   id: String = UUID.randomUUID.toString,
                   created: LocalTime = LocalTime.now):
  def execute: Int = f(n)

case class Computed(value: Int,
                    id: String = UUID.randomUUID.toString,
                    created: LocalTime = LocalTime.now)

case class Events(events: List[Computed] = Nil):
  def add(event: Computed): Events = copy(event :: events)
  def list: List[Computed] = events

case object Result
case object Snapshot
case object Shutdown

class ComputeActor extends PersistentActor with ActorLogging:
  var events = Events()

  override def persistenceId: String = "computed-event-persistence-id"

  def updateState(event: Computed): Unit =
    events = events.add(event)

  override def receiveCommand: Receive =
    case command: Compute => persistAsync(Computed(command.execute))(updateState)
    case Snapshot => saveSnapshot(events)
    case SaveSnapshotSuccess(metadata) => log.info(s"*** Computer snapshot successful: $metadata")
    case SaveSnapshotFailure(_, reason) => throw reason
    case Result => sender() ! events.list
    case Shutdown => context.stop(self)

  override def receiveRecover: Receive =
    case event: Computed => updateState(event)
    case SnapshotOffer(_, snapshot: Events) => events = snapshot
    case RecoveryCompleted => log.info("*** Computer snapshot recovery completed.")

class PersistenceTest extends AnyFunSuite with BeforeAndAfterAll:
  given timeout: Timeout = Timeout(20 seconds)
  val system = ActorSystem.create("persistence", Conf.config)
  val computeActor = system.actorOf(Props[ComputeActor](), name = "computer-actor")
  given dispatcher: ExecutionContext = system.dispatcher

  def fibonacci(n: Int): Int =
    @tailrec
    def loop(n: Int, a: Int, b: Int): Int = n match {
      case 0 => a
      case _ => loop(n - 1, b, a + b)
    }
    loop(n, 0, 1)

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 3 seconds)
    ()

  test("persistence") {
    for (n <- 1 to 10) computeActor ! Compute(fibonacci, n)
    Thread.sleep(3000)

    computeActor ! Snapshot
    Thread.sleep(3000)

    val events = Await.result( (computeActor ? Result).mapTo[List[Computed]], 15 seconds)
    println("fibonacci computed events:")
    events.foreach(event => println(s"id: ${event.id} created: ${event.created} value: ${event.value}"))
    assert(events.size >= 10)

    computeActor ! Shutdown
  }