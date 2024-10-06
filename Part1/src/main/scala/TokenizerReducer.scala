import java.io.IOException
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import play.api.libs.json.Json
import scala.collection.JavaConverters._
import java.io.{BufferedWriter, OutputStreamWriter}
import org.apache.hadoop.fs.{FileSystem, Path}

class TokenizerReducer extends Reducer[Text, Text, Text, Text] {
  private var csvData: List[String] = List()

  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    // Get the list of tokenized strings
    val tokens = values.asScala.toList

    // Count frequency (number of tokens)
    val frequency = tokens.length

    // Create CSV line
    val csvLine = s"${key.toString},${tokens.mkString(" ")},$frequency"
    csvData = csvData :+ csvLine
  }

  override def cleanup(context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    // Write the CSV output
    val fs = FileSystem.get(context.getConfiguration)
    val csvOutputPath = new Path("s3://hw1-raunak/output.csv")
    val csvStream = fs.create(csvOutputPath)
    val writer = new BufferedWriter(new OutputStreamWriter(csvStream))

    // Write CSV headers
    writer.write("Word,Token,Frequency\n")

    // Write CSV data
    csvData.foreach { line =>
      writer.write(line + "\n")
    }

    writer.close()
  }
}