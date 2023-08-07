package objektwerks.http

import java.time.LocalTime

import org.apache.pekko.http.scaladsl.model.StatusCodes.OK
import org.apache.pekko.http.scaladsl.server.Directives.*

import spray.json.*

object NowJsonCodecs extends DefaultJsonProtocol:
  given JsonFormat[Now] = jsonFormat1(Now.apply(_))

case class Now(time: String = LocalTime.now.toString)

trait NowService {
  import org.apache.pekko.http.scaladsl.marshalling.*
  import org.apache.pekko.http.scaladsl.unmarshalling.*
  import spray.json.*
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