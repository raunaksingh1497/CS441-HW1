import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat

object TokenizerDriver {
  def Tokenize(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: TokenizerDriver <inputPath> <outputPath>")
      System.exit(1)
    }

    val inputPath = args(0)
    val outputPath = args(1)

    // Print input paths
    println(s"Input path: $inputPath")
    println(s"Output path: $outputPath")

    val conf = new Configuration()
    conf.set("fs.defaultFS", "s3://hw1-raunak") // Ensure the default filesystem is set

    val job = Job.getInstance(conf, "Tokenization")
    job.setJarByClass(classOf[TokenizerMapper])
    job.setMapperClass(classOf[TokenizerMapper])
    job.setReducerClass(classOf[TokenizerReducer])

    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])

    FileInputFormat.addInputPath(job, new Path(inputPath))
    FileOutputFormat.setOutputPath(job, new Path(outputPath))

    // Print job status
    println("Starting the job...")
    if (job.waitForCompletion(true)) {
      println("Job completed successfully.")
    } else {
      println("Job failed.")
      System.exit(1)
    }
  }
}