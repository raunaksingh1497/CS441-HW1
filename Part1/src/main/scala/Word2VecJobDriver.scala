import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat

import java.io.{BufferedReader, FileWriter, InputStreamReader}

object Word2VecJobDriver {
  @throws[Exception]
  def main(args: Array[String]): Unit = {
    println("Starting Word2VecJobDriver...")

    if (args.length < 2) {
      println("Usage: Word2VecJobDriver <inputPath> <outputPath>")
      System.exit(1)
    }

    // val tokenFilePath = args(0)  // Input path from MainApp
    // val outputPath = args(1)

    // Define your input and output paths
    // val tokenFilePath = "/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/src/main/resources/output/part-r-00000"
    // val outputPath = "/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/src/main/resources/vector-embedding/"

    val tokenFilePath = "s3a://hw1-raunak/output/part-r-00000"
    val outputPath = "s3a://hw1-raunak/word2vec"

    println(s"Input path: $tokenFilePath")
    println(s"Output path: $outputPath")

    // Setup the job configuration
    val conf = new Configuration()
    val job = Job.getInstance(conf, "Word2Vec Training")
    conf.set("fs.defaultFS", "s3a://hw1-raunak")  // Use s3a for better performance and compatibility

    job.setJarByClass(Word2VecJobDriver.getClass)
    println("Job configuration setup completed.")

    // Set Mapper and Reducer
    job.setMapperClass(classOf[Word2VecMapper])
    job.setReducerClass(classOf[Word2VecReducer])
    println("Mapper and Reducer classes set.")

    // Set output types
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])

    // Input and Output paths
    FileInputFormat.addInputPath(job, new Path(tokenFilePath))  // Input path
    FileOutputFormat.setOutputPath(job, new Path(outputPath))    // Output path
    println(s"Input and Output paths set: $tokenFilePath -> $outputPath")

    // Wait for the job to complete
    println("Waiting for job completion...")
    val success = job.waitForCompletion(true)
    if (success) {
      println("Job completed successfully.")
      System.exit(0)
    } else {
      println("Job failed.")
      System.exit(1)
    }
  }
}