import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.slf4j.LoggerFactory

import java.net.URI
import scala.jdk.CollectionConverters.IterableHasAsScala

class Word2VecReducer extends Reducer[Text, Text, Text, Text] {

  private var word2Vec: Word2Vec = _
  private val logger = LoggerFactory.getLogger(classOf[Word2VecReducer])

  def setupModel(context: Context): Unit = {
    val modelS3Path = new Path("s3a://hw1-raunak/ronny/model/word2Vec-model.bin")  // S3 model path
    val conf = context.getConfiguration
    conf.set("fs.defaultFS", "s3a://hw1-raunak")
    val fs = FileSystem.get(new URI("s3a://hw1-raunak"), conf)

    // Define a local path where you temporarily store the model
    val localModelPath = new java.io.File("/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/word2vec_model.bin")

    // Copy the file from S3 to the local file system
    fs.copyToLocalFile(false, modelS3Path, new Path(localModelPath.getPath), true)
    logger.info(s"Model successfully downloaded from $modelS3Path and loaded from local path: ${localModelPath.getPath}")

    // Now read the model from the local file
    word2Vec = WordVectorSerializer.readWord2VecModel(localModelPath)
    logger.info("Word2Vec model loaded successfully.")
  }

  def reduce(key: Text, values: java.lang.Iterable[Text], context: Context): Unit = {
    // Process each token in the input
    values.asScala.foreach { value =>
      val tokens = value.toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toInt)

      // Generate the embedding for each token
      val embeddings = tokens.map { token =>
        val tokenStr = token.toString
        val vector = Option(word2Vec.getWordVector(tokenStr))
        vector.map(_.mkString(",")).getOrElse("Not Found") // Handle case when token is not in the vocabulary
      }

      // Output the token and its embeddings using foreach instead of a for loop
      tokens.indices.foreach { i =>
        logger.info(s"Writing token: ${tokens(i)} with embedding: ${embeddings(i)}")
        context.write(new Text(tokens(i).toString), new Text(embeddings(i)))
      }
    }
  }

  def cleanupReducer(context: Context): Unit = {
    // Optional: Any cleanup tasks, if necessary
  }
}