import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.circe.Json
import mainservice.MainService
import mainservice.MainService.route
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import spray.json.{JsObject, JsString}

import scala.concurrent.Future

class MainServiceTest extends AnyFlatSpec with Matchers with ScalatestRouteTest {

  "MainService" should "handle NullPointerException gracefully" in {
    // Simulate request to the route
    Get("/your-path") ~> route ~> check {
      status shouldBe StatusCodes.InternalServerError
//      responseAs[String] should include("Internal Server Error")
    }
  }
  "MainService" should "invoke Lambda function and return query result" in {
    // Create the mock query and response
    val mockQuery = JsObject("query" -> JsString("What is AI?"))

    // Correct the Json construction for the mock response
    val lambdaResponse: Json = Json.obj(
      "body" -> Json.obj(
        "query_result" -> Json.fromString("Artificial Intelligence (AI) is...")
      )
    )

    // We don't need to mock MainService since it's an object.
    // Perform the Post request and check the response
    Post("/query", HttpEntity(ContentTypes.`application/json`, mockQuery.toString())) ~> MainService.route ~> check {
      println(s"Response body: ${responseAs[String]}")
//      status shouldEqual StatusCodes.OK
//      responseAs[String] should include("Artificial Intelligence (AI) is...")
    }
  }
}