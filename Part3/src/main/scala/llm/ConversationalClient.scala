package llm

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.models.OllamaResult
import io.github.ollama4j.utils.Options
import scala.concurrent.{ExecutionContextExecutor, Future, Await}
import scala.concurrent.duration._
import io.circe._, io.circe.parser._
import java.util.Collections
import com.typesafe.config.ConfigFactory

object ConversationalClient {
  // Load configuration from application.conf
  private val config = ConfigFactory.load()

  val ollamaAPI = new OllamaAPI(config.getString("ollama.host")) // Ollama server setup
  ollamaAPI.setRequestTimeoutSeconds(config.getInt("ollama.request-timeout-seconds"))

  implicit val system: ActorSystem = ActorSystem("ConversationalClientSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Logger setup
  val log = Logging(system, getClass)

  def main(args: Array[String]): Unit = {
    val initialQuery = config.getString("json.initial-query")
    continueConversation(initialQuery)
  }

  // Function to continue the conversation
  def continueConversation(query: String): Unit = {
    // Send the query to LLMServer and wait for the response
    val responseFromLLM = Await.result(sendQueryToLLM(query), 500.seconds)

    // Log the response from LLMServer
    log.info(s"Response from LLMServer: $responseFromLLM")

    // Process response using Ollama to generate follow-up query
    val followUpQuery = generateFollowUpQuery(responseFromLLM)
    log.info(s"Follow-up query generated by Ollama: $followUpQuery")

    // Continue conversation if termination condition is not met
    if (!terminationConditionMet(responseFromLLM)) {
      continueConversation(followUpQuery)
    }
  }

  // Send query to LLMServer
  def sendQueryToLLM(query: String): Future[String] = {
    val llmServerUrl = config.getString("llmserver.url")
    val sanitizedQuery = query.replace("\n", "\\n") // Escape newline characters
    val entity = HttpEntity(ContentTypes.`application/json`, s"""{"query":"$sanitizedQuery"}""")

    log.info(s"Sending sanitized query to LLMServer: $sanitizedQuery")

    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = llmServerUrl, entity = entity))
      .flatMap { response =>
        response.entity.toStrict(5.seconds).map { strictEntity =>
          strictEntity.data.utf8String
        }
      }
      .recover {
        case ex: Exception =>
          log.error(s"Failed to communicate with LLMServer: ${ex.getMessage}")
          s"Error: ${ex.getMessage}"
      }
  }

  // Generate follow-up query using Ollama
  def generateFollowUpQuery(response: String): String = {
    // Attempt to parse the response as JSON
    parse(response) match {
      case Left(error) =>
        // Log the parsing error and return a meaningful message
        log.error(s"Malformed response: ${error.getMessage}")
        return s"Failed to process malformed response: $response"
      case Right(json) =>
        // Extract the "text" field from the JSON
        val text = json.hcursor.downField("text").as[String] match {
          case Right(value) => value
          case Left(_) =>
            // Handle case where the "text" field is missing or invalid
            log.error("Error: Missing 'text' field in response")
            return "Error: Missing 'text' field in response"
        }

        // Create the prompt for Ollama with the extracted text
        val prompt = s"How can you respond to the statement: \"$text\""

        // Log the query being sent to Ollama
        log.info(s"Sending query to Ollama: $prompt")

        try {
          // Send the query to Ollama
          val result: OllamaResult = ollamaAPI.generate(config.getString("ollama.model"), prompt, false, new Options(Collections.emptyMap()))
          result.getResponse
        } catch {
          case e: Exception =>
            log.error(s"Failed to generate follow-up query: ${e.getMessage}")
            s"Failed to generate follow-up query: ${e.getMessage}"
        }
    }
  }

  // Termination condition logic
  def terminationConditionMet(response: String): Boolean = {
    // Add logic to stop based on certain conditions
    false
  }
}