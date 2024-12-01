package llmserver

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.http.scaladsl.model.HttpMethods._
import scala.concurrent.duration._
import spray.json._
import com.typesafe.config.ConfigFactory

object LLMServer extends App {

  // Load configuration from application.conf
  private val config = ConfigFactory.load()

  // Akka setup
  implicit val system: ActorSystem = ActorSystem("LLMServerSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Logger setup
  val log = Logging(system, getClass)

  // Read configuration values
  val mainServiceUrl = config.getString("mainservice.url")
  val serverHost = config.getString("llmserver.serverHost")
  val serverPort = config.getInt("llmserver.serverPort")
  val httpTimeout = config.getInt("llmserver.httpTimeout").seconds

  val route =
    path("query") {
      post {
        entity(as[String]) { query =>
          val responseFuture: Future[String] = sendQueryToMainService(query)
          onComplete(responseFuture) {
            case Success(response) =>
              // Log the response from MainService
              log.info(s"Response in LLMServer: $response")
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

  // Send the query to MainService via HTTP
  def sendQueryToMainService(query: String): Future[String] = {
    val sanitizedQuery = query.replace("\n", "\\n") // Escape newline characters
    val entity = HttpEntity(ContentTypes.`application/json`, JsObject("query" -> JsString(sanitizedQuery)).compactPrint)

    log.info(s"The query sent to MainService: $sanitizedQuery")
    log.info(s"The entity sent to MainService: $entity")

    Http().singleRequest(HttpRequest(POST, mainServiceUrl, entity = entity))
      .flatMap { response =>
        response.entity.toStrict(httpTimeout).map { strictEntity =>
          val responseData = strictEntity.data.utf8String
          log.info(s"Response from MainService: $responseData")
          responseData
        }
      }
      .recover {
        case ex: Exception =>
          log.error(s"Failed to communicate with MainService: ${ex.getMessage}")
          s"Error: ${ex.getMessage}"
      }
  }

  // Start the LLMServer HTTP server
  val serverBinding = Http().bindAndHandle(route, serverHost, serverPort)

  serverBinding.onComplete {
    case Success(binding) =>
      log.info(s"LLMServer started at ${binding.localAddress}")
    case Failure(exception) =>
      log.error(s"LLMServer could not start: ${exception.getMessage}")
      system.terminate()
  }
}