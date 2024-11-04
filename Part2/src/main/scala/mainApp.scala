import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object mainApp {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName(ConfigLoader.sparkAppName)
      .master("local[*]")
      .config("spark.hadoop.fs.defaultFS", "file:///")
      .getOrCreate()

    try {
      val inputFilePath = ConfigLoader.localInputFilePath
//      val modelPath = ConfigLoader.s3ModelPath
      val modelPath = ConfigLoader.localModelPath

      // Step 1: Generate sliding windows tensor from input data
      val slidingWindowsTensor = SlidingWindowLLMExample.processSlidingWindows(spark, inputFilePath)


      // Step 2: Train a model using the generated tensor
      SparkLLMTraining.trainWithTensor(spark, slidingWindowsTensor)

      // Example input embeddings for inference after training
      val inputEmbeddings = Array(
        Array(0.2830190658569336, -0.7829341888427734, -0.448542058467865, -0.7401682734489441, -0.0205706600099802, 0.1267770677804947, -0.8933035135269165, 0.45400765538215637, 0.8079659938812256, -0.8172789812088013),
        Array(-0.39992204308509827, -0.7774288654327393, 0.015934407711029053, -0.12076327204704285, 0.4200984835624695, 0.20958319306373596, -0.6598931550979614, -0.4673583507537842, 0.2960030734539032, -0.16988906264305115)
      )

      // Step 3: Run inference on the trained model
      val predictedEmbedding = LLMInference.predictNextEmbedding(modelPath, inputEmbeddings)
      println("Predicted next embedding: " + predictedEmbedding.mkString("[", ", ", "]"))


    } catch {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}