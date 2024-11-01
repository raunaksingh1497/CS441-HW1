import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object SlidingWindowLLMExample {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    // Set up Spark configuration and context
    val conf = new SparkConf()
      .setAppName("Sliding Window LLM Dataset")
      .setMaster("local[*]")
      .set("spark.hadoop.fs.defaultFS", "file:///")               // Use local filesystem
      .set("spark.local.dir", "/Users/ronny/tmp/spark")           // Set Spark's temporary directory to a local path
    val sc = new SparkContext(conf)

    // File paths for input
    val inputFilePath = "file:///Users/ronny/Desktop/CS441Cloud/Scala/Spark/src/main/resources/word2vec/part-r-00000"

    // Define dimensions and sliding window parameters
    val windowSize = 15    // Maximum tokens (rows) to include in each window
    val embeddingSize = 10 // Embedding size (E) per token
    val overlap = 7        // Number of tokens to overlap between windows

    // Load and parse data from the input file
    val inputData = parseInputToMatrix(inputFilePath, sc)

    // Apply the sliding window algorithm and convert to tensor
    val slidingWindowsTensor = generateSlidingWindowsAsTensor(inputData, windowSize, overlap)

    // Print the shape of the tensor
    println(s"Sliding Windows Tensor Shape: ${slidingWindowsTensor.shape.mkString(", ")}")

    // Stop the Spark context
    sc.stop()

    SparkLLMTraining.trainWithTensor(slidingWindowsTensor)
  }

  // Function to parse input file and organize it into a 2D matrix
  def parseInputToMatrix(filePath: String, sc: SparkContext): Array[Array[Double]] = {
    val dataRDD: RDD[Array[Double]] = sc.textFile(filePath).map { line =>
      val parts = line.split("\t")
      if (parts.length > 1) {
        try {
          parts(1).split(",").map(_.toDouble)
        } catch {
          case e: NumberFormatException =>
            logger.error(s"Failed to convert embedding to Double: ${parts(1)}", e)
            Array.empty[Double]
        }
      } else {
        logger.warn(s"Line format unexpected: $line")
        Array.empty[Double]
      }
    }

    dataRDD.collect()
  }

  // Function to generate sliding windows and convert them to an INDArray tensor
  def generateSlidingWindowsAsTensor(data: Array[Array[Double]], windowSize: Int, overlap: Int): INDArray = {
    val step = windowSize - overlap

    // Generate sliding windows from the data array
    val slidingWindows = data.sliding(windowSize, step).toArray

    // Create a 3D tensor with shape: (numWindows, windowSize, embeddingSize)
    val tensor = Nd4j.create(1, windowSize, data.head.length)

    // Fill the tensor with data from each sliding window
    for (j <- slidingWindows.head.indices) {
      for (k <- slidingWindows.head(j).indices) {
        tensor.putScalar(Array(0, j, k), slidingWindows.head(j)(k))  // Use the first sliding window (index 0)
      }
    }
    tensor
  }
}