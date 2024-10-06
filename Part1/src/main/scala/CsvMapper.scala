//import org.apache.hadoop.io.{LongWritable, Text}
//import org.apache.hadoop.mapreduce.Mapper
//
//class CsvMapper extends Mapper[LongWritable, Text, Text, LongWritable] {
//
//  val one = new LongWritable(1)
//  val wordText = new Text()
//
//  // Tokenizer function
//  def tokenizeText(line: String): Array[String] = {
//    line.toLowerCase.replaceAll("[^a-z0-9]", " ").split("\\s+").filter(_.nonEmpty)
//  }
//
//
//  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, LongWritable]#Context): Unit = {
//    // Tokenize each line of text
//    val words = tokenizeText(value.toString)
//
//    words.foreach { word =>
//      wordText.set(word)
//      context.write(wordText, one)  // Emit each word with a count of 1
//    }
//  }
//}
