//import org.apache.hadoop.conf.Configuration
//import org.apache.hadoop.fs.Path
//import org.apache.hadoop.io.{LongWritable, Text}
//import org.apache.hadoop.mapreduce.Job
//import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
//import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
//
//object CsvJobDriver {
//  @throws[Exception]
//  def main(args: Array[String]): Unit = {
//    // Set input and output paths directly
////    val inputPath = "/Users/ronny/Desktop/CS 441 Cloud/Scala/W2V-EMR/src/main/resources/input"
////    val outputPath = "/Users/ronny/Desktop/CS 441 Cloud/Scala/W2V-EMR/src/main/resources/output"
//
//    //HDFS PATH
//    //    val inputPath = "/ronny/input"
//    //    val outputPath = "/ronny/output"
//    //EMR path
//        val inputPath = "s3://hw1-raunak/input/"
//        val outputPath = "s3://hw1-raunak/csv/"
//
//    // Configure and create the Hadoop job
//    val conf = new Configuration()
//    val job = Job.getInstance(conf, "Csv")
//    conf.set("fs.defaultFS", "s3://hw1-raunak")
//    // Set the default filesystem to local (file:///)
//    //    conf.set("fs.defaultFS", "file:///")
//
//    // Set number of reduce tasks (e.g., 3 reduce tasks)
//    conf.setInt("mapreduce.job.reduces", 3)
//
//    // Set input split size (reduce split size for more map tasks)
//    //    conf.set("mapreduce.input.fileinputformat.split.maxsize", "134217728")
//
//    // Set the jar file
//    job.setJarByClass(classOf[CsvMapper])
//
//    // Set the Mapper and Reducer classes
//    job.setMapperClass(classOf[CsvMapper])
//    job.setReducerClass(classOf[CsvReducer])
//
//    // Set the output key and value types for the MapReduce job
//    job.setOutputKeyClass(classOf[Text])
//    job.setOutputValueClass(classOf[LongWritable])
//
//    // Specify input and output paths
//    FileInputFormat.addInputPath(job, new Path(inputPath))
//    FileOutputFormat.setOutputPath(job, new Path(outputPath))
//
//    // Run the job and wait for it to finish
//    try {
//      System.exit(if (job.waitForCompletion(true)) 0 else 1)
//    } catch {
//      case e: Exception =>
//        e.printStackTrace()  // Print stack trace for debugging
//        System.exit(1)
//    }
//  }
//}
