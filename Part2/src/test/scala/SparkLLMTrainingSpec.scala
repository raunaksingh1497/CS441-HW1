import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.api.ndarray.INDArray
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.scalatest.BeforeAndAfterEach

class SparkLLMTrainingSpec extends AnyFunSuite with BeforeAndAfterEach{

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
      spark = null // Clear reference for garbage collection
    }
  }

  test("createModel should return a MultiLayerNetwork") {
    val tensor: INDArray = Nd4j.create(Array.ofDim[Float](10, 5, 3))
    val model: MultiLayerNetwork = SparkLLMTraining.createModel(tensor)
    assert(model != null)
    assert(model.getLayer(0).conf().getLayer.getClass.getSimpleName == "LSTM")
    assert(model.getLayer(1).conf().getLayer.getClass.getSimpleName == "RnnOutputLayer")
  }

  test("captureGradientNormsLocally should return a valid gradient norm") {
    val tensor = Nd4j.rand(10, 5, 3)
    val trainingData = SparkLLMTraining.createTrainingDataFromTensor(spark.sparkContext, tensor)
    val model = SparkLLMTraining.createModel(tensor)
    val gradientNorm = SparkLLMTraining.captureGradientNormsLocally(model, trainingData)
    assert(gradientNorm > 0.0, "Gradient norm should be positive.")
  }
}