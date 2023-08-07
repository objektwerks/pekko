package objektwerks.http

import com.typesafe.config.ConfigFactory

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object NowApp extends App with NowService {
  val logger = LoggerFactory.getLogger(getClass)
  val conf = ConfigFactory.load("now.app.conf")

  given system: ActorSystem = ActorSystem.create(conf.getString("server.name"), conf)
  given executor: ExecutionContext = system.dispatcher

  val host = conf.getString("server.host")
  val port = conf.getInt("server.port")
  val server = Http()
    .newServerAt(host, port)
    .bindFlow(routes)

  logger.info(s"*** NowApp started at http://$host:$port/\nPress RETURN to stop...")

  StdIn.readLine()
  server
    .flatMap(_.unbind())
    .onComplete { _ =>
      system.terminate()
      logger.info("*** NowApp stopped.")
    }
}