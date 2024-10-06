import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.slf4j.{Logger, LoggerFactory}

import java.io.{BufferedReader, FileWriter, InputStreamReader}

object Word2VecJobDriver {
  // Initialize the logger
  private val logger: Logger = LoggerFactory.getLogger(Word2VecJobDriver.getClass)

  @throws[Exception]
  def main(args: Array[String]): Unit = {
    logger.info("Starting Word2VecJobDriver...")

    if (args.length < 2) {
      logger.error("Usage: Word2VecJobDriver <inputPath> <outputPath>")
      System.exit(1)
    }

    val tokenFilePath = "s3a://hw1-raunak/output/part-r-00000"
    val outputPath = "s3a://hw1-raunak/word2vec"

    logger.info(s"Input path: $tokenFilePath")
    logger.info(s"Output path: $outputPath")

    // Setup the job configuration
    val conf = new Configuration()
    val job = Job.getInstance(conf, "Word2Vec Training")
    conf.set("fs.defaultFS", "s3a://hw1-raunak") // Use s3a for better performance and compatibility

    job.setJarByClass(Word2VecJobDriver.getClass)
    logger.info("Job configuration setup completed.")

    // Set Mapper and Reducer
    job.setMapperClass(classOf[Word2VecMapper])
    job.setReducerClass(classOf[Word2VecReducer])
    logger.info("Mapper and Reducer classes set.")

    // Set output types
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])

    // Input and Output paths
    FileInputFormat.addInputPath(job, new Path(tokenFilePath))  // Input path
    FileOutputFormat.setOutputPath(job, new Path(outputPath))    // Output path
    logger.info(s"Input and Output paths set: $tokenFilePath -> $outputPath")

    // Wait for the job to complete
    logger.info("Waiting for job completion...")
    val success = job.waitForCompletion(true)
    if (success) {
      logger.info("Job completed successfully.")
    } else {
      logger.error("Job failed.")
      System.exit(1)
    }
  }
}