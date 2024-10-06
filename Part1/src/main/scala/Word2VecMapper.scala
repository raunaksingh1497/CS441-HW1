import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.deeplearning4j.models.word2vec.Word2Vec
import org.slf4j.{Logger, LoggerFactory}

import java.io.{BufferedReader, IOException, InputStreamReader}

class Word2VecMapper extends Mapper[LongWritable, Text, Text, Text] { // Changed Text to LongWritable

  // Initialize the logger
  private val logger: Logger = LoggerFactory.getLogger(classOf[Word2VecMapper])

  @throws[IOException]
  @throws[InterruptedException]
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    logger.info("Starting map function.")

    // Load tokenized data
    val tokenizedData: List[List[Long]] = readTokenizedFile("s3a://hw1-raunak/output/part-r-00000", context)

    val tokenizedSentences: List[List[String]] = tokenizedData.map(_.map(_.toString))

    // Create a TokenizedSentenceIterator from the parsed tokenized data
    val sentenceIterator = new TokenizedSentenceIterator(tokenizedSentences)

    // Build Word2Vec model using the sentence iterator
    logger.info("Building Word2Vec model...")
    val word2Vec = new Word2Vec.Builder()
      .minWordFrequency(5)
      .iterations(100)
      .layerSize(10)
      .seed(42)
      .windowSize(5)
      .iterate(sentenceIterator)
      .build()

    // Train the model
    logger.info("Training Word2Vec model...")
    word2Vec.fit()

    logger.info("Word2Vec model training completed.")

    // Output embeddings for each token in your output context
    tokenizedSentences.flatten.distinct.foreach { token =>
      val embedding: Array[Double] = word2Vec.getWordVector(token)
      if (embedding != null) {
        logger.info(s"Writing token: $token with embedding: ${embedding.mkString(",")}")
        context.write(new Text(token), new Text(embedding.mkString(","))) // Write token and its embedding
      } else {
        logger.warn(s"Embedding for token: $token is null.")
      }
    }
  }

  // Helper function to read and parse the tokenized.txt file as List[List[Long]]
  def readTokenizedFile(filePath: String, context: Mapper[LongWritable, Text, Text, Text]#Context): List[List[Long]] = {
    logger.info(s"Reading tokenized file from path: $filePath")
    val conf: Configuration = context.getConfiguration
    conf.set("fs.defaultFS", "s3a://hw1-raunak") // Use s3a for better performance
    val fs: FileSystem = FileSystem.get(conf)
    val path = new Path(filePath)

    // Read from HDFS
    val inputStream = fs.open(path)
    val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    val fileContent = bufferedReader.lines().toArray.mkString // Read all lines

    val parsedTokens: List[List[Long]] = fileContent
      .stripPrefix("[[")
      .stripSuffix("]]")
      .split("],\\s*\\[")
      .toList
      .map(_.split(",").map(_.trim).map(token => token.replaceAll("[^0-9]", "").toLong).toList)

    inputStream.close() // Ensure to close the stream

    logger.info(s"Successfully read and parsed tokens from $filePath")
    parsedTokens
  }
}