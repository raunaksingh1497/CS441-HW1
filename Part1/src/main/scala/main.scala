import org.slf4j.{Logger, LoggerFactory}

object MainApp {
  // Initialize the logger
  private val logger: Logger = LoggerFactory.getLogger(MainApp.getClass)

  def main(args: Array[String]): Unit = {
    // Define paths for tokenization
    val tokenizationInputPath = "s3://hw1-raunak/input/" // AWS path for input data
    val tokenizationOutputPath = "s3://hw1-raunak/output" // AWS path for output data
    val tokenizationCSVOutputPath = "s3://hw1-raunak/csv/" // AWS path for output CSV

    // Define paths for Word2Vec
    val word2VecInputPath = tokenizationOutputPath
    val word2VecOutputPath = "s3://hw1-raunak/word2vec"

    val cosineSimilarityInputPath = "s3://hw1-raunak/word2vec"
    val cosineSimilarityOutputPath = "s3://hw1-raunak/cosine-similarity"

    // Call TokenizerDriver
    try {
      logger.info("Running TokenizerDriver...")
      TokenizerDriver.Tokenize(Array(tokenizationInputPath, tokenizationOutputPath, tokenizationCSVOutputPath))
      logger.info("TokenizerDriver completed successfully.")
    } catch {
      case e: Exception =>
        logger.error("Error running TokenizerDriver: {}", e.getMessage)
        System.exit(1) // Exit if the TokenizerDriver fails
    }

    // Call Word2VecDriver after CsvJobDriver completes
    try {
      logger.info("Running Word2VecDriver...")
      Word2VecJobDriver.main(Array(word2VecInputPath, word2VecOutputPath))
      logger.info("Word2VecDriver completed successfully.")
    } catch {
      case e: Exception =>
        logger.error("Error running Word2VecDriver: {}", e.getMessage)
        System.exit(1)  // Exit if the Word2VecDriver fails
    }

    // Uncomment to call CosineSimilarityDriver if needed
    try {
      logger.info("Running CosineSimilarityDriver...")
      CosineSimilarityDriver.main(Array(cosineSimilarityInputPath, cosineSimilarityOutputPath))
      logger.info("CosineSimilarityDriver completed successfully.")
    } catch {
      case e: Exception =>
        logger.error("Error running CosineSimilarityDriver: {}", e.getMessage)
        System.exit(1)  // Exit if the CosineSimilarityDriver fails
    }
  }
}