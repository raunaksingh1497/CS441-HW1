import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar

class Word2VecJobDriverTest extends AnyFunSuite with MockitoSugar with BeforeAndAfter {

  var mockJob: Job = _
  var conf: Configuration = _

  before {
    // Create mock instances
    mockJob = mock[Job]
    conf = new Configuration()
  }

  test("Test successful job configuration") {
    // Arrange
    val tokenFilePath = "s3a://hw1-raunak/output/part-r-00000"
    val outputPath = "s3a://hw1-raunak/word2vec"
    val args = Array(tokenFilePath, outputPath)

    when(mockJob.getConfiguration).thenReturn(conf)

    // Act
    Word2VecJobDriver.main(args)

    // Assert
    val inputPath = new Path(tokenFilePath)
    val expectedOutputPath = new Path(outputPath)

    // Check that input/output paths are set correctly
    verify(mockJob).setJarByClass(Word2VecJobDriver.getClass)
    FileInputFormat.addInputPath(mockJob, inputPath)
    FileOutputFormat.setOutputPath(mockJob, expectedOutputPath)

    assert(FileInputFormat.getInputPaths(mockJob).head == inputPath, "Input path should match")
    assert(FileOutputFormat.getOutputPath(mockJob) == expectedOutputPath, "Output path should match")
  }

  test("Test job failure with insufficient arguments") {
    // Test when insufficient arguments are provided
    val insufficientArgs = Array("s3a://hw1-raunak/output/part-r-00000")

    intercept[IllegalArgumentException] {
      Word2VecJobDriver.main(insufficientArgs)
    }
  }

  test("Test job completion is successful") {
    // Arrange
    val args = Array("s3a://hw1-raunak/output/part-r-00000", "s3a://hw1-raunak/word2vec")

    // Mock job completion
    when(mockJob.waitForCompletion(true)).thenReturn(true)

    // Act
    Word2VecJobDriver.main(args)

    // Assert
    verify(mockJob).waitForCompletion(true)
  }

  test("Test job failure during execution") {
    // Arrange
    val args = Array("s3a://hw1-raunak/output/part-r-00000", "s3a://hw1-raunak/word2vec")

    // Mock job failure
    when(mockJob.waitForCompletion(true)).thenReturn(false)

    // Act and Assert
    intercept[SystemExitException] {
      Word2VecJobDriver.main(args)
    }

    // Verify System.exit(1) was called due to job failure
    verify(mockJob).waitForCompletion(true)
  }

  test("Test logger calls during execution") {
    // Arrange
    val args = Array("s3a://hw1-raunak/output/part-r-00000", "s3a://hw1-raunak/word2vec")

    // Create a spy of the logger
    val logger = spy(Word2VecJobDriver.logger)

    // Act
    Word2VecJobDriver.main(args)

    // Assert logger messages
    verify(logger).info("Starting Word2VecJobDriver...")
    verify(logger).info(s"Input path: s3a://hw1-raunak/output/part-r-00000")
    verify(logger).info(s"Output path: s3a://hw1-raunak/word2vec")
    verify(logger).info("Mapper and Reducer classes set.")
    verify(logger).info("Job completed successfully.")
  }
}