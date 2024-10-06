import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingType, IntArrayList}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.slf4j.{Logger, LoggerFactory}

import java.io.IOException

class TokenizerMapper extends Mapper[LongWritable, Text, Text, Text] {
  private val logger: Logger = LoggerFactory.getLogger(classOf[TokenizerMapper])
  val encoding: Encoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE)

  @throws[IOException]
  @throws[InterruptedException]
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    logger.info(s"Processing line with key: ${key.get}")

    // Split input line into sentences
    val sentences = value.toString.split("""(?<=[.!?])\s+""")
    logger.info(s"Split input into ${sentences.length} sentences.")

    sentences.foreach { sentence =>
      val tokenizedSentence: IntArrayList = encoding.encode(sentence)
      val tokensString = tokenizedSentence.toArray.mkString(" ")

      logger.debug(s"Tokenized sentence: $sentence -> Tokens: $tokensString")
      context.write(new Text(sentence), new Text(tokensString))
    }
  }
}