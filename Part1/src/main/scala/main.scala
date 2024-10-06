object MainApp {
  def main(args: Array[String]): Unit = {

    // Define paths for tokenization
    // val tokenizationInputPath = "/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/src/main/resources/input"
    // val tokenizationOutputPath = "/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/src/main/resources/output"
    // val tokenizationCSVOutputPath = "/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/src/main/resources/output/output.csv"

    // HDFS Path
    // val tokenizationInputPath = "/ronny/input"    // HDFS path for input data
    // val tokenizationOutputPath = "/ronny/output"  // HDFS path for output data
    // val tokenizationCSVOutputPath = "/ronny/output/output.csv"  // HDFS path for output CSV

    // Amazon S3
    val tokenizationInputPath = "s3://hw1-raunak/input/"    // AWS path for input data
    val tokenizationOutputPath = "s3://hw1-raunak/output"  // AWS path for output data
    val tokenizationCSVOutputPath = "s3://hw1-raunak/output/output.csv"  // AWS path for output CSV

    // Define paths for Word2Vec
    val word2VecInputPath = tokenizationOutputPath

    // S3 path (output from TokenizerDriver)
    val word2VecOutputPath = "s3://hw1-raunak/word2vec"

    // Call TokenizerDriver
    try {
      println("Running TokenizerDriver...")
      TokenizerDriver.Tokenize(Array(tokenizationInputPath, tokenizationOutputPath, tokenizationCSVOutputPath))
      println("TokenizerDriver completed successfully.")
    } catch {
      case e: Exception =>
        println("Error running TokenizerDriver: " + e.getMessage)
        System.exit(1)  // Exit if the TokenizerDriver fails
    }

    // Call Word2VecDriver after TokenizerDriver completes
    try {
      println("Running Word2VecDriver...")
      Word2VecJobDriver.main(Array(word2VecInputPath, word2VecOutputPath))
      println("Word2VecDriver completed successfully.")
    } catch {
      case e: Exception =>
        println("Error running Word2VecDriver: " + e.getMessage)
        System.exit(1)  // Exit if the Word2VecDriver fails
    }

    // // Call CosineSimilarityDriver after Word2VecDriver completes
    // try {
    //   println("Running CosineSimilarityDriver...")
    //   CosineSimilarityDriver.main(Array(cosineSimilarityInputPath, cosineSimilarityOutputPath))
    //   println("CosineSimilarityDriver completed successfully.")
    // } catch {
    //   case e: Exception =>
    //     println("Error running CosineSimilarityDriver: " + e.getMessage)
    //     System.exit(1)  // Exit if the CosineSimilarityDriver fails
    // }
  }
}