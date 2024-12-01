import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Directives._
import org.scalatest.flatspec.AnyFlatSpec
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.util.Timeout
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.concurrent.Future
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import scala.concurrent.duration._

class ConversationalClientText extends AnyFlatSpec with ScalatestRouteTest {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Simulating a mock response from MainService
  def mockSendQueryToMainService(query: String): Future[String] = {
    if (query == """{ "invalidJson" """) {
      // Simulate failure for malformed JSON
      Future.failed(new Exception("Malformed JSON"))
    } else {
      Future.successful(s"Mocked response for query: $query")
    }
  }

  "ConversationalClient" should "handle malformed JSON responses gracefully" in {
    // Test body for handling malformed JSON
    val malformedResponse = """{ "invalidJson" """

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

    // Test sending a malformed JSON response
    Post("/query", malformedResponse) ~> route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  it should "respond with a successful response when a valid query is sent" in {
    val validQuery = "How do cats express love?"
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
    Post("/query", validQuery) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Mocked response for query: How do cats express love?"
    }
  }
}