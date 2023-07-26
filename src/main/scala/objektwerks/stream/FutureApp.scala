package objektwerks

import com.typesafe.config.ConfigFactory

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.*
import scala.io.StdIn
import scala.language.postfixOps

@main def runFutureApp: Unit =
  val numbers = 10
  val parallelism = Runtime.getRuntime.availableProcessors

  given system: ActorSystem = ActorSystem.create("future-app", ConfigFactory.load("app.conf"))
  given dispatcher: ExecutionContextExecutor = system.dispatcher
  println("*** akka system started")

  println(s"*** squaring numbers with mapAsync parallelism set to: $parallelism ...")
  Source(1 to numbers)
    .mapAsync(parallelism) { number =>
      Future { // simulate async io
        println(s"*** $number squared = ${number * number}")
      }
    }
    .runWith(Sink.ignore)
  println(s"*** once all squared numbers have been printed, depress RETURN key to shutdown app")

  StdIn.readLine()

  Await.result(system.terminate(), 10 seconds)
  println("*** akka system terminated")
  println("*** see log at /target/app.log")
  println("*** app shutdown")