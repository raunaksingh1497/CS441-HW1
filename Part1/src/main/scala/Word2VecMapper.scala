import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.deeplearning4j.models.word2vec.Word2Vec

import java.io.{BufferedReader, IOException, InputStreamReader}

class Word2VecMapper extends Mapper[LongWritable, Text, Text, Text] { // Changed Text to LongWritable

  @throws[IOException]
  @throws[InterruptedException]
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {

    // Load tokenized data
    // val tokenizedData: List[List[Long]] = readTokenizedFile("/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/src/main/resources/output/part-r-00000")
    // val tokenizedData: List[List[Long]] = readTokenizedFile("/ronny/output/part-r-00000", context)

    val tokenizedData: List[List[Long]] = readTokenizedFile("s3a://hw1-raunak/output/part-r-00000", context)

    val tokenizedSentences: List[List[String]] = tokenizedData.map(_.map(_.toString))

    // Create a TokenizedSentenceIterator from the parsed tokenized data
    val sentenceIterator = new TokenizedSentenceIterator(tokenizedSentences)

    // Build Word2Vec model using the sentence iterator
    val word2Vec = new Word2Vec.Builder()
      .minWordFrequency(5)
      .iterations(100)
      .layerSize(10)
      .seed(42)
      .windowSize(5)
      .iterate(sentenceIterator)
      .build()

    // Train the model
    word2Vec.fit()

    // Save the model to HDFS
    // val conf: Configuration = context.getConfiguration
    // val fs: FileSystem = FileSystem.get(conf)
    //
    // // Define the HDFS output path for the model
    // val hdfsPath = new Path("/ronny/model/word2Vec-model.bin")
    // val outputStream: OutputStream = fs.create(hdfsPath)
    //
    // try {
    //   WordVectorSerializer.writeWord2VecModel(word2Vec, outputStream)
    // } finally {
    //   outputStream.close() // Ensure the stream is closed properly
    // }

    // Save the model for later use
    // WordVectorSerializer.writeWord2VecModel(word2Vec, new java.io.File("word2vec_model.bin"))

    // Output embeddings for each token in your output context
    tokenizedSentences.flatten.distinct.foreach { token =>
      val embedding: Array[Double] = word2Vec.getWordVector(token)
      if (embedding != null) {
        println(s"Writing token: $token with embedding: ${embedding.mkString(",")}")
        context.write(new Text(token), new Text(embedding.mkString(","))) // Write token and its embedding
      }
    }
  }

  // Helper function to read and parse the tokenized.txt file as List[List[Int]]
  def readTokenizedFile(filePath: String, context: Mapper[LongWritable, Text, Text, Text]#Context): List[List[Long]] = {
    val conf: Configuration = context.getConfiguration
    conf.set("fs.defaultFS", "s3a://hw1-raunak")  // Use s3a for better performance
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

    parsedTokens
  }
}