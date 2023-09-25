package objektwerks.http

import com.typesafe.config.ConfigFactory

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

final class NowServiceTest extends AnyWordSpec with Matchers with ScalatestRouteTest with BeforeAndAfterAll with NowService:
  import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
  import NowJsonCodecs.given

  given ActorSystem = ActorSystem.create("now", ConfigFactory.load("test.conf"))
  val server = Http()
    .newServerAt("localhost", 0)
    .bindFlow(routes)

  override protected def afterAll(): Unit =
    server
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  "NowService" should {
    "get and post" in {
      Get("/api/v1/now") ~> routes ~> check {
        status shouldBe StatusCodes.OK
      }
      Post("/api/v1/now", Now()) ~> routes ~> check {
        status shouldBe StatusCodes.OK
      }
    }
  }
