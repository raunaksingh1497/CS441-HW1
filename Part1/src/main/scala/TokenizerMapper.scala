import java.io.IOException
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingType, IntArrayList}

class TokenizerMapper extends Mapper[LongWritable, Text, Text, Text] {
  val encoding: Encoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE)

  @throws[IOException]
  @throws[InterruptedException]
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val sentences = value.toString.split("""(?<=[.!?])\s+""") // Split input into sentences

    sentences.foreach { sentence =>
      val tokenizedSentence: IntArrayList = encoding.encode(sentence)
      val tokensString = tokenizedSentence.toArray.mkString(" ") // Join tokens with space

      // Split sentence into words for frequency counting
      val words = sentence.split("\\s+")
      words.foreach { word =>
        context.write(new Text(word), new Text(tokensString)) // Original key-value output
      }

      // Output data for CSV (word, token, frequency)
      context.write(new Text(sentence), new Text(tokensString)) // Original key-value output
    }
  }
}