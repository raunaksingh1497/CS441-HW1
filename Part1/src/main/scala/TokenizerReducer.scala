import java.io.IOException
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import play.api.libs.json.Json
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.JavaConverters._

class TokenizerReducer extends Reducer[Text, Text, Text, Text] {
  private val logger: Logger = LoggerFactory.getLogger(classOf[TokenizerReducer]) // Initialize logger
  private var allTokenizedSentences: List[List[Int]] = List()

  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    logger.info(s"Reducing key: ${key.toString}")

    // Collect all tokenized sentences for each key
    values.asScala.foreach { sentence =>
      val tokenizedSentence: List[Int] = sentence.toString.split(" ").toList.map(_.toInt)
      allTokenizedSentences = allTokenizedSentences :+ tokenizedSentence
      logger.debug(s"Tokenized sentence added: $tokenizedSentence") // Log each tokenized sentence added
    }

    logger.info(s"Total tokenized sentences collected for key '${key.toString}': ${allTokenizedSentences.size}")
  }

  override def cleanup(context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    // Convert the entire list of lists to JSON after the reduce phase
    val jsonOutput = Json.stringify(Json.toJson(allTokenizedSentences))

    // Write the final JSON output as a single string
    context.write(null, new Text(jsonOutput))

    logger.info(s"Final JSON output written for key: ${jsonOutput.take(100)}...") // Log the start of the JSON output (first 100 characters)
  }
}