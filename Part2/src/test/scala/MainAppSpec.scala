import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.SparkSession
import java.nio.file.{Files, Paths}

class MainAppSpec extends AnyFunSuite {

  test("MainApp runs without exceptions") {
    val localInputFilePath = ConfigLoader.testInput
    val modelPath = ConfigLoader.localModelPath // Ensure this is set to a valid path in your configuration

    // Create Spark session locally for the test
    val spark = SparkSession.builder()
      .appName("LocalSparkAppTest")
      .master("local[*]")
      .getOrCreate()

    // Ensure paths exist before running test
    assert(Files.exists(Paths.get(localInputFilePath)), s"Input file path does not exist: $localInputFilePath")
    assert(Files.exists(Paths.get(modelPath).getParent), s"Model output directory does not exist: ${Paths.get(modelPath).getParent}")

    // Run MainApp main method within a try-catch to ensure no exceptions
    try {
      mainApp.main(Array(localInputFilePath, modelPath)) // Corrected to MainApp.main
    } catch {
      case e: Exception =>
        fail(s"MainApp threw an exception: ${e.getMessage}")
    } finally {
      spark.stop() // Clean up Spark session
    }
  }
}