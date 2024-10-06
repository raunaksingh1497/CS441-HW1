import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec

import java.net.URI
import scala.jdk.CollectionConverters.IterableHasAsScala

class Word2VecReducer extends Reducer[Text, Text, Text, Text] {

  private var word2Vec: Word2Vec = _

  def setupModel(context: Context): Unit = {
    // Load the trained Word2Vec model
    // val modelPath = "/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/src/main/resources/word2Vec-model.bin"
    // val modelPath = "s3://hw1-raunak/output/word2Vec-model.zip"
    // val word2Vec = WordVectorSerializer.readWord2VecModel(modelPath)

    // val conf: Configuration = context.getConfiguration
    // val fs: FileSystem = FileSystem.get(conf)
    //
    // // Open the file as an InputStream
    // val inputStream: InputStream = fs.open(modelHdfsPath)
    //
    // try {
    //   // Load the model using InputStream
    //   word2Vec = WordVectorSerializer.readWord2VecModel(inputStream)
    // } finally {
    //   // Ensure that the input stream is closed
    //   inputStream.close()
    // }

    // val modelHdfsPath = new Path("/ronny/model/word2Vec-model.bin") // Your HDFS model path
    // val conf = context.getConfiguration
    // val fs = FileSystem.get(conf)

    val modelS3Path = new Path("s3a://hw1-raunak/ronny/model/word2Vec-model.bin")  // S3 model path
    val conf = context.getConfiguration

    // Ensure AWS credentials are set correctly and configure S3A as the filesystem
    conf.set("fs.defaultFS", "s3a://hw1-raunak")
    val fs = FileSystem.get(new URI("s3a://hw1-raunak"), conf)

    // Define a local path where you temporarily store the model
    val localModelPath = new java.io.File("/Users/ronny/Desktop/CS441Cloud/Assignment/killmeLLM/Part1/word2vec_model.bin")

    // Copy the file from HDFS to the local file system
    // fs.copyToLocalFile(modelS3Path, new Path(localModelPath.getPath))

    // Copy the file from S3 to the local file system
    fs.copyToLocalFile(false, modelS3Path, new Path(localModelPath.getPath), true)

    // Now read the model from the local file
    word2Vec = WordVectorSerializer.readWord2VecModel(localModelPath)
    println(s"Model successfully downloaded from $modelS3Path and loaded from local path: ${localModelPath.getPath}")
  }

  def reduce(key: Text, values: java.lang.Iterable[Text], context: Context): Unit = {
    // Process each token in the input
    for (value <- values.asScala) {
      val tokens = value.toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toInt)

      // Generate the embedding for each token
      val embeddings = tokens.map(token => {
        // Convert the token (Integer) to String if your model works with String tokens
        val tokenStr = token.toString
        // Get the vector embedding
        val vector = Option(word2Vec.getWordVector(tokenStr))
        vector.map(_.mkString(",")).getOrElse("Not Found") // Handle case when token is not in the vocabulary
      })

      // Output the token and its embeddings
      for (i <- tokens.indices) {
        println(s"Writing token: ${tokens(i)} with embedding: ${embeddings(i)}")
        context.write(new Text(tokens(i).toString), new Text(embeddings(i)))
      }
    }
  }

  def cleanupReducer(context: Context): Unit = {
    // Optional: Any cleanup tasks, if necessary
  }
}