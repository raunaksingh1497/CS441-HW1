//import org.apache.hadoop.io.{LongWritable, Text}
//import org.apache.hadoop.mapreduce.Reducer
//import com.opencsv.CSVWriter
//import java.io.{File, FileWriter}
//import scala.jdk.CollectionConverters._
//
//class CsvReducer extends Reducer[Text, LongWritable, Text, Text] {
//
//  var csvWriter: CSVWriter = _  // CSVWriter instance for writing the CSV file
//
//  // Path to the output CSV file
////  val outputPath = "/Users/ronny/Desktop/CS 441 Cloud/Scala/W2V-EMR/src/main/resources/output/output.csv"
//  val outputPath = "s3://hw1-raunak/csv/"
//
//  //  val outputPath = "/ronny/output"
//
//  //      val outputPath = "s3://hw1-raunak/output/output.csv"
//
//  //   Setup method: Initializes CSVWriter and writes the header to the CSV file
//  override def setup(context: Reducer[Text, LongWritable, Text, Text]#Context): Unit = {
//    // Initialize the CSVWriter to append mode (so you can accumulate results)
//    val writer = new FileWriter(outputPath, true)
//    csvWriter = new CSVWriter(writer)
//
//    // Write the CSV header
//    val header = Array("Word", "Token", "Frequency")
//    csvWriter.writeNext(header)
//  }
//
//  // Reduce method: Write word, token, and frequency to CSV
//  override def reduce(key: Text, values: java.lang.Iterable[LongWritable], context: Reducer[Text, LongWritable, Text, Text]#Context): Unit = {
//    val word = key.toString
//    val token = generateToken(word).toString
//
//    // Sum the occurrences of the word (i.e., calculate frequency)
//    val frequency = values.asScala.foldLeft(0L)(_ + _.get)
//
//    // Create the row for the CSV
//    val csvRow = Array(word, token, frequency.toString)
//
//    // Write the row to the CSV file
//    csvWriter.writeNext(csvRow)
//
//    // Optionally, you can still emit it to Hadoop context if needed
//    context.write(null, new Text(csvRow.mkString(",")))
//  }
//
//  //   Cleanup method: Close the CSVWriter
//  override def cleanup(context: Reducer[Text, LongWritable, Text, Text]#Context): Unit = {
//    if (csvWriter != null) {
//      csvWriter.close()
//    }
//  }
//
//  // Token generation function
//  def generateToken(word: String): Int = {
//    word.hashCode  // Simple hash as token
//  }
//}