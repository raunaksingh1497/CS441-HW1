import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.log4j.Logger

object CosineSimilarityDriver {
  // Initialize the Logger
  val logger: Logger = Logger.getLogger(CosineSimilarityDriver.getClass)

  @throws[Exception]
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      logger.error("Insufficient arguments. Please provide input and output paths.")
      System.exit(1)
    }

//    val inputPath = args(0)
//    val outputPath = args(1)
    val inputPath = "s3://hw1-raunak/word2vec"
    val outputPath = "s3://hw1-raunak/cosine-similarity"
    logger.info(s"Input Path: $inputPath")
    logger.info(s"Output Path: $outputPath")

    try {
      // Setup the job configuration
      val conf = new Configuration()
      val job = Job.getInstance(conf, "Cosine similarity")// Create a new Configuration object
      conf.set("fs.defaultFS", "s3a://hw1-raunak") // Ensure this is set correctly
      job.setJarByClass(CosineSimilarityDriver.getClass)
      // Log job configuration
      logger.info("Job configuration set up successfully")

      // Set Mapper and Reducer
      job.setMapperClass(classOf[CosineSimilarityMapper])
      job.setReducerClass(classOf[CosineSimilarityReducer])

      logger.info("Mapper and Reducer classes set")

      // Set output types
      job.setOutputKeyClass(classOf[Text])
      job.setOutputValueClass(classOf[Text])

      // Log key/value output types
      logger.info("Output key and value types set")

      // Input and Output paths
      FileInputFormat.addInputPath(job, new Path(inputPath))  // Input path
      FileOutputFormat.setOutputPath(job, new Path(outputPath))  // Output path

      logger.info(s"Input path set to: $inputPath")
      logger.info(s"Output path set to: $outputPath")

      // Wait for the job to complete
      val jobCompleted = job.waitForCompletion(true)
      if (jobCompleted) {
        logger.info("Job completed successfully")
      } else {
        logger.error("Job failed")
      }
      System.exit(if (jobCompleted) 0 else 1)

    } catch {
      case e: Exception =>
        logger.error("Error occurred while setting up or running the job", e)
        System.exit(1)
    }
  }
}