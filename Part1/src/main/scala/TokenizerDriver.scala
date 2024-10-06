import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.slf4j.{Logger, LoggerFactory}

object TokenizerDriver {
  private val logger: Logger = LoggerFactory.getLogger(TokenizerDriver.getClass)

  def Tokenize(args: Array[String]): Unit = {
    if (args.length < 3) {
      logger.error("Usage: TokenizerDriver <inputPath> <outputPath> <csvOutputPath>")
      System.exit(1)
    }

    val inputPath = args(0)
    val outputPath = args(1)
    val csvOutputPath = args(2)

    // Log input paths
    logger.info(s"Input path: $inputPath")
    logger.info(s"Output path: $outputPath")
    logger.info(s"CSV output path: $csvOutputPath")

    val conf = new Configuration()
    val job = Job.getInstance(conf, "Tokenization")
    conf.set("fs.defaultFS", "s3://hw1-raunak")

    job.setJarByClass(classOf[TokenizerMapper])
    job.setMapperClass(classOf[TokenizerMapper])
    job.setReducerClass(classOf[TokenizerReducer])

    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])  // Mapper and Reducer both output Text

    FileInputFormat.addInputPath(job, new Path(inputPath))
    FileOutputFormat.setOutputPath(job, new Path(outputPath))

    // Log job status
    logger.info("Starting the job...")
    if (job.waitForCompletion(true)) {
      logger.info("Job completed successfully.")
    } else {
      logger.error("Job failed.")
      System.exit(1)
    }
  }
}