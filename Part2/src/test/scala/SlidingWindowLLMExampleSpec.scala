import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite
import org.nd4j.linalg.api.ndarray.INDArray
import org.scalatest.BeforeAndAfterEach

class SlidingWindowLLMExampleSpec extends AnyFunSuite with BeforeAndAfterEach{

  var spark: SparkSession = _

  override def beforeEach(): Unit = {
    // Initialize SparkSession before each test
    spark = SparkSession.builder()
      .appName("SparkLLMTrainingSpec")
      .master("local[*]")
      .getOrCreate()
  }

  override def afterEach(): Unit = {
    // Stop SparkSession after each test
    if (spark != null) {
      spark.stop()
      SparkSession.clearActiveSession()
      spark = null // Clear reference for garbage collection
    }
  }

  test("processSlidingWindows should return a valid tensor") {
    val inputFilePath = ConfigLoader.testInput;
    val tensor: INDArray = SlidingWindow.processSlidingWindows(spark,inputFilePath)
    assert(tensor != null)
    assert(tensor.shape().length == 3, "Tensor should have 3 dimensions")
  }

  test("parseInputToMatrix should parse file correctly") {
    val sc = spark.sparkContext
    val inputFilePath = ConfigLoader.testInput;
    val matrix = SlidingWindow.parseInputToMatrix(inputFilePath, sc)
    assert(matrix.length > 0, "Matrix should not be empty")
    assert(matrix.head.length > 0, "Matrix rows should not be empty")
  }
}