import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.apache.log4j.Logger

import scala.collection.mutable.ListBuffer

class CosineSimilarityReducer extends Reducer[Text, Text, Text, Text] {

  // Logger setup
  val logger: Logger = Logger.getLogger(classOf[CosineSimilarityReducer])

  override def setup(context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    logger.info("Reducer setup phase started.")
    // Additional setup tasks (if needed) can be done here
    logger.info("Reducer setup phase completed.")
  }

  override def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    logger.info(s"Reducing key: $key")

    // Create a buffer to hold all the outputs for a specific token
    val results = ListBuffer[String]()

    // Iterate over all values (cosine similarity results from the mapper)
    val iterator = values.iterator()
    while (iterator.hasNext) {
      val value = iterator.next().toString
      logger.debug(s"Processing value for key $key: $value")
      results += value
    }

    // Log the total number of values processed for this key
    logger.info(s"Total values processed for key $key: ${results.size}")

    // Write out each result in the desired format
    results.foreach { result =>
      logger.info(s"Writing result for key $key: $result")
      context.write(key, new Text(result)) // Output each result
    }
  }

  override def cleanup(context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    logger.info("Reducer cleanup phase completed.")
  }
}