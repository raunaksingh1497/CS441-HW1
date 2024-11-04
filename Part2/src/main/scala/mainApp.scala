import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object mainApp {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName(ConfigLoader.sparkAppName)
      .master("local[*]")
      .config("spark.hadoop.fs.defaultFS", "file:///")
      .getOrCreate()

    logger.info("Spark session created successfully.")

    try {
      val inputFilePath = ConfigLoader.localInputFilePath
      val modelPath = ConfigLoader.localModelPath

      logger.info(s"Input file path: $inputFilePath")
      logger.info(s"Model path: $modelPath")

      // Step 1: Generate sliding windows tensor from input data
      logger.info("Generating sliding windows tensor from input data.")
      val slidingWindowsTensor = SlidingWindow.processSlidingWindows(spark, inputFilePath)
      logger.info("Sliding windows tensor generated successfully.")

      // Step 2: Train a model using the generated tensor
      logger.info("Starting model training with the generated tensor.")
      SparkModelTraining.trainWithTensor(spark, slidingWindowsTensor)
      logger.info("Model training completed successfully.")

      // Example input embeddings for inference after training
      val inputEmbeddings = Array(
        Array(0.2830190658569336, -0.7829341888427734, -0.448542058467865, -0.7401682734489441, -0.0205706600099802, 0.1267770677804947, -0.8933035135269165, 0.45400765538215637, 0.8079659938812256, -0.8172789812088013),
        Array(-0.39992204308509827, -0.7774288654327393, 0.015934407711029053, -0.12076327204704285, 0.4200984835624695, 0.20958319306373596, -0.6598931550979614, -0.4673583507537842, 0.2960030734539032, -0.16988906264305115)
      )

      // Step 3: Run inference on the trained model
      logger.info("Running inference on the trained model.")
      val predictedEmbedding = LLMInference.predictNextEmbedding(modelPath, inputEmbeddings)
      logger.info("Inference completed successfully.")
      println("Predicted next embedding: " + predictedEmbedding.mkString("[", ", ", "]"))

    } catch {
      case e: Exception =>
        logger.error(s"An error occurred: ${e.getMessage}", e)
        e.printStackTrace()
    } finally {
      spark.stop()
      logger.info("Spark session stopped.")
    }
  }
}