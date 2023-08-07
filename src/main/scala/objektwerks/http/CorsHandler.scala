package objektwerks.http

import org.apache.pekko.http.scaladsl.model.HttpMethods._
import org.apache.pekko.http.scaladsl.model.headers._
import org.apache.pekko.http.scaladsl.model.HttpResponse
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.{Directive0, Route}

/**
 * See: https://dzone.com/articles/handling-cors-in-akka-http
 */
trait CorsHandler:
  val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With")
  )

  def corsHandler(route: Route): Route = addAccessControlHeaders { preflightRequestHandler ~ route }

  def addAccessControlHeaders: Directive0 = respondWithHeaders( corsResponseHeaders )

  def preflightRequestHandler: Route = options {
    complete( HttpResponse()
      .withHeaders( `Access-Control-Allow-Methods`(GET, POST, PUT, PATCH, DELETE, OPTIONS) ) )
  }

  def addCORSHeaders(response: HttpResponse): HttpResponse = response.withHeaders( corsResponseHeaders )