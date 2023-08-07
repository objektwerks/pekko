package objektwerks.http

import java.time.LocalTime

import de.heikoseeberger.akkahttpupickle.UpickleSupport._

case class Now(time: String = LocalTime.now.toString)

object Now {
  import upickle.default._

  implicit val nowRW: ReadWriter[Now] = macroRW
}

trait NowService {
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