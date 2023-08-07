package objektwerks.http

import java.time.LocalTime

import org.apache.pekko.http.scaladsl.model.StatusCodes.OK
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import spray.json.{DefaultJsonProtocol, JsonFormat}

object NowJsonCodecs extends SprayJsonSupport with DefaultJsonProtocol:
  given JsonFormat[Now] = jsonFormat1(Now.apply(_))

case class Now(time: String = LocalTime.now.toString)

trait NowService {
  import NowJsonCodecs.given

  val getNow = get {
    complete(OK -> Now())
  }
  val postNow = post {
    entity(as[Now]) { _ => complete(OK) }
  }
  val api = pathPrefix("api" / "v1" / "now") {
    getNow ~ postNow
  }
  val index = path("") {
    getFromResource("public/index.html")
  }
  val resources = get {
    getFromResourceDirectory("public")
  }
  val routes = api ~ index ~ resources
}