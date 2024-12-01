package mainservice

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling._
import play.api.libs.json._
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.{InvokeRequest, InvokeResponse}
import com.typesafe.config.ConfigFactory
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.concurrent.Await
import scala.concurrent.duration._
import com.example.protobuf.{Request, Response} 

object MainService extends App {
  // Akka HTTP setup
  implicit val system: ActorSystem = ActorSystem("MainServiceSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Logger setup
  val log = Logging(system, getClass)

  // Load configurations from application.conf
  private val config = ConfigFactory.load()

  // AWS configuration
  private val accessKey = config.getString("aws.access-key")
  private val secretKey = config.getString("aws.secret-key")
  private val region = config.getString("aws.region")
  private val lambdaFunctionName = config.getString("aws.lambda-function-name")

  // Lambda client setup
  private val lambdaClient = LambdaClient.builder()
    .region(Region.of(region))
    .credentialsProvider(StaticCredentialsProvider.create(
      AwsBasicCredentials.create(accessKey, secretKey)
    ))
    .build()

  // Define the implicit unmarshaller for JsValue
  implicit val jsValueUnmarshaller: FromRequestUnmarshaller[JsValue] = Unmarshaller.withMaterializer[HttpRequest, JsValue] { implicit ec => materializer =>
    // Convert the request entity to a strict entity and then parse it as a JsValue
    (request: HttpRequest) =>
      request.entity.toStrict(config.getDuration("http.strict-entity-timeout").getSeconds.seconds)(materializer).map { strictEntity =>
        Json.parse(strictEntity.data.utf8String)
      }
  }

  def handleStructuredData(request: String): JsValue = {
    // Protobuf handling
    val protoRequest = Json.obj(
      "query" -> request
    )

    // Log the structured (simulated Protobuf) data
    log.info(s"Structured Data: $protoRequest")

    // Simulate structured response (as if it was generated from Protobuf)
    val protoResponse = Json.obj(
      "query_result" -> s"Processed response for query: $request",
      "status_code" -> 200,
      "error_message" -> JsNull,
      "error_type" -> JsNull,
      "request_id" -> "simulated-request-id",
      "stack_trace" -> Json.arr("simulated-stack-trace")
    )

    protoResponse
  }

  object Protobuf {
    case class Request(query: String)
    case class Response(queryResult: String, statusCode: Int, errorMessage: Option[String])

    def serializeRequest(request: Request): Array[Byte] = {

      val serializedData = s"""{"query": "${request.query}"}"""

      serializedData.getBytes("UTF-8")
    }

    def deserializeResponse(response: String): Response = {

      Response(queryResult = "Protobuf resoonse", statusCode = 200, errorMessage = None)
    }
  }
  // Define the HTTP route
  val route =
    path("query") {
      post {
        entity(as[JsValue]) { json =>
          // Log the incoming JSON to inspect it
          log.info(s"Received Protobuf: $json")

          // Extract the "query" field from the JSON request body
          (json \ config.getString("json.request-key")).asOpt[String] match {
            case Some(query) =>
              // Call the Lambda function asynchronously with the query
              val responseFuture: Future[String] = invokeLambda(query)

              onComplete(responseFuture) {
                case Success(response) =>
                  complete(HttpEntity(ContentTypes.`application/json`, response))
                case Failure(exception) =>
                  log.error(s"Error invoking Lambda: ${exception.getMessage}")
                  complete(HttpResponse(StatusCodes.InternalServerError, entity = s"Error invoking Lambda: ${exception.getMessage}"))
              }

            case None =>
              log.warning(s"Missing '${config.getString("json.request-key")}' field in request")
              complete(HttpResponse(StatusCodes.BadRequest, entity = s"Missing '${config.getString("json.request-key")}' field"))
          }
        }
      }
    }

  // Start the HTTP server
  val serverBinding = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))

  serverBinding.onComplete {
    case Success(binding) =>
      log.info(s"MainService started at ${binding.localAddress}")
    case Failure(exception) =>
      log.error(s"MainService could not start: ${exception.getMessage}")
      system.terminate()
  }

  // Keep the server alive until terminated
  Await.result(serverBinding, Duration.Inf)

  // Asynchronous invocation of Lambda
  def invokeLambda(query: String): Future[String] = {
    val requestProto = Json.obj(config.getString("json.request-key") -> query).toString()
    val sdkBytesPayload = SdkBytes.fromUtf8String(requestProto)
    val invokeRequest = InvokeRequest.builder()
      .functionName(lambdaFunctionName)
      .payload(sdkBytesPayload)
      .build()

    Future {
      try {
        val response: InvokeResponse = lambdaClient.invoke(invokeRequest)
        val responseProto = response.payload().asUtf8String()
        log.info(s"Lambda response: $responseProto")

        //parsing proto to json
        val jsonResponse = Json.parse(responseProto)
        val bodyString = (jsonResponse \ "body").as[String]
        val bodyJson = Json.parse(bodyString)
        val queryResult = (bodyJson \ config.getString("json.response-key")).as[String]
        queryResult
      } catch {
        case e: Exception =>
          log.error(s"Error invoking Lambda: ${e.getMessage}")
          s"Error invoking Lambda: ${e.getMessage}"
      }
    }
  }
}