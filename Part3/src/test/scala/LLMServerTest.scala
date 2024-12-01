package llmserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{RouteTest, ScalatestRouteTest}
import akka.stream.ActorMaterializer
import org.scalatest.flatspec.AnyFlatSpec
import akka.http.scaladsl.model.HttpMethods._
import scala.concurrent.Future
import akka.util.Timeout
import org.scalatest.matchers.should.Matchers
import spray.json._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Success, Failure}
import scala.concurrent.duration._

class LLMServerTest extends AnyFlatSpec with Matchers with ScalatestRouteTest {

  // Use the implicitly provided system and materializer from RouteTest
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Simulating a mock response from MainService
  def mockSendQueryToMainService(query: String): Future[String] = {
    Future.successful(s"Mocked response for query: $query")
  }

  // Test Case 1: Check if the route responds successfully when a valid query is sent
  "LLMServer" should "respond with status OK when a valid query is sent" in {
    val route =
      path("query") {
        post {
          entity(as[String]) { query =>
            val responseFuture: Future[String] = mockSendQueryToMainService(query)
            onComplete(responseFuture) {
              case Success(_) =>
                complete(StatusCodes.OK)
              case Failure(_) =>
                complete(HttpResponse(
                  status = StatusCodes.InternalServerError,
                  entity = "Error"
                ))
            }
          }
        }
      }

    // Test sending a valid query
    Post("/query", "How do cats express love?") ~> route ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  // Test Case 2: Check if the response body matches the mocked response
  it should "respond with the mocked response body for a valid query" in {
    val route =
      path("query") {
        post {
          entity(as[String]) { query =>
            val responseFuture: Future[String] = mockSendQueryToMainService(query)
            onComplete(responseFuture) {
              case Success(response) =>
                complete(HttpEntity(ContentTypes.`application/json`, response))
              case Failure(exception) =>
                complete(HttpResponse(
                  status = StatusCodes.InternalServerError,
                  entity = s"Error: ${exception.getMessage}"
                ))
            }
          }
        }
      }

    // Test sending a valid query
    Post("/query", "How do cats express love?") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "Mocked response for query: How do cats express love?"
    }
  }
}