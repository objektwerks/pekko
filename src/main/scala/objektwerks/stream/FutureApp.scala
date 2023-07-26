package objektwerks

import com.typesafe.config.ConfigFactory

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

object FutureApp {
  def main(args: Array[String]): Unit = {
    val numbers = 10
    val parallelism = Runtime.getRuntime.availableProcessors

    implicit val system: ActorSystem = ActorSystem.create("future-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher: ExecutionContext = system.dispatcher
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
  }
}