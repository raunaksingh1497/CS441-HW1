import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.slf4j.LoggerFactory

object SlidingWindow {
  private val logger = LoggerFactory.getLogger(getClass)

  // Method to process sliding windows and return the tensor
  def processSlidingWindows(spark: SparkSession, inputFilePath: String): INDArray = {
    val sc = spark.sparkContext
    // Log the start of the sliding window processing
    logger.info(s"Starting sliding window processing for file: $inputFilePath")

    // Define dimensions and sliding window parameters from ConfigLoader
    val windowSize = ConfigLoader.windowSize
    val overlap = ConfigLoader.overlap

    logger.debug(s"Configured window size: $windowSize, overlap: $overlap")

    // Load and parse data from the input file
    val inputData = parseInputToMatrix(inputFilePath, sc)
    logger.info(s"Input data matrix loaded with ${inputData.length} rows.")

    // Apply the sliding window algorithm and convert to tensor
    val slidingWindowsTensor = generateSlidingWindowsAsTensor(inputData, windowSize, overlap)
    logger.info(s"Generated sliding windows tensor with shape: ${slidingWindowsTensor.shape.mkString(", ")}")

    slidingWindowsTensor
  }

  // Function to parse input file and organize it into a 2D matrix
  def parseInputToMatrix(filePath: String, sc: SparkContext): Array[Array[Double]] = {
    logger.info(s"Parsing input file: $filePath")

    val dataRDD = sc.textFile(filePath).map { line =>
      val parts = line.split("\t")
      if (parts.length > 1) {
        try {
          parts(1).split(",").map(_.toDouble)
        } catch {
          case e: NumberFormatException =>
            logger.warn(s"Skipping line due to number format exception: $line", e)
            Array.empty[Double]
        }
      } else {
        logger.warn(s"Unexpected line format (missing data after tab): $line")
        Array.empty[Double]
      }
    }

    val dataMatrix = dataRDD.collect()
    logger.info(s"Data matrix parsed with ${dataMatrix.length} rows.")

    // Log an example row for debugging purposes (if data is not too large)
    if (dataMatrix.nonEmpty) {
      logger.debug(s"First row of parsed data matrix: ${dataMatrix.head.mkString(", ")}")
    }

    dataMatrix
  }

  def generateSlidingWindowsAsTensor(
                                      data: Array[Array[Double]],
                                      windowSize: Int,
                                      overlap: Int
                                    ): INDArray = {
    logger.info("Generating sliding windows tensor from data matrix.")

    // Validate that data contains rows of expected length
    if (data.isEmpty || data.head.isEmpty) {
      logger.warn("Data matrix is empty or contains empty rows.")
      return Nd4j.create(0) // Return an empty tensor to avoid errors in processing
    }

    val step = windowSize - overlap
    logger.debug(s"Using step size: $step")

    val slidingWindows = data.sliding(windowSize, step).toArray
    logger.info(s"Total sliding windows generated: ${slidingWindows.length}")

    val tensor = Nd4j.create(slidingWindows.length, windowSize, data.head.length)
    for (i <- slidingWindows.indices; j <- slidingWindows(i).indices; k <- slidingWindows(i)(j).indices) {
      tensor.putScalar(Array(i, j, k), slidingWindows(i)(j)(k))
    }

    logger.info(s"Sliding windows tensor created with shape: ${tensor.shape.mkString(", ")}")
    tensor
  }
}