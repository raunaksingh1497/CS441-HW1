import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.apache.log4j.Logger
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.mutable
import scala.math.sqrt

class CosineSimilarityMapper extends Mapper[LongWritable, Text, Text, Text] {

  // Logger setup
  val logger: Logger = Logger.getLogger(classOf[CosineSimilarityMapper])

  // Hold all tokens and embeddings in memory
  val allTokensAndEmbeddings = mutable.Map[String, Array[Double]]()

  // Setup method to initialize all tokens and embeddings
  override def setup(context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    logger.info("Starting the setup phase.")

    // Load all token embeddings from HDFS

    val conf: Configuration = context.getConfiguration
    conf.set("fs.defaultFS", "s3a://hw1-raunak") // Ensure this is set correctly
    val fs = FileSystem.get(conf)

    val path = new Path("s3a://hw1-raunak/word2vec/part-r-00000")

    val stream = fs.open(path)
    val lines = scala.io.Source.fromInputStream(stream).getLines()

    // Read each line, split into token and embedding
    lines.foreach { line =>
      val parts = line.split("\\t")
      if (parts.length == 2) {
        val token = parts(0)
        val embedding = parts(1).split(",").map(_.toDouble)
        allTokensAndEmbeddings += (token -> embedding)
        logger.info(s"Loaded token: $token with embedding.")
      }
    }
    stream.close()
    logger.info("Setup phase completed.")
  }

  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    // Log the incoming key-value pair
    logger.info(s"Processing key: $key, value: ${value.toString}")

    // Parse the input line to extract token and its embedding
    val line = value.toString.split("\\t")
    if (line.length == 2) {
      val token = line(0)
      val embedding = line(1).split(",").map(_.toDouble)

      // Store the token and its embedding in the map
      allTokensAndEmbeddings += (token -> embedding)
      logger.info(s"Stored token: $token with embedding: ${embedding.mkString(",")}")

      // Calculate cosine similarity for this token with every other token
      allTokensAndEmbeddings.foreach { case (otherToken, otherEmbedding) =>
        if (token != otherToken) {  // Avoid calculating cosine similarity for the same token
          val cosineSimilarity = calculateCosineSimilarity(embedding, otherEmbedding)

          // Take the absolute value of cosine similarity
          val absCosineSimilarity = math.abs(cosineSimilarity)
          logger.debug(s"Cosine similarity between $token and $otherToken: $absCosineSimilarity")

          // Classify the cosine similarity value into A, B, C, or D
          val classification = classifyCosineSimilarity(absCosineSimilarity)
          logger.debug(s"Classification for similarity $absCosineSimilarity: $classification")

          // Format the output
          val similarityString = f"$token $otherToken $absCosineSimilarity%.4f $classification"

          // Emit the result
          context.write(new Text(token), new Text(similarityString))
          logger.info(s"Emitted result for $token and $otherToken: $similarityString")
        }
      }
    } else {
      logger.warn(s"Invalid input format: ${value.toString}")
    }
  }

  // Cosine similarity function
  def calculateCosineSimilarity(vecA: Array[Double], vecB: Array[Double]): Double = {
    logger.debug(s"Calculating cosine similarity between vectors: ${vecA.mkString(",")} and ${vecB.mkString(",")}")
    val dotProduct = vecA.zip(vecB).map { case (a, b) => a * b }.sum
    val magnitudeA = sqrt(vecA.map(a => a * a).sum)
    val magnitudeB = sqrt(vecB.map(b => b * b).sum)
    val result = dotProduct / (magnitudeA * magnitudeB)
    logger.debug(s"Cosine similarity result: $result")
    result
  }

  // Classification based on cosine similarity value
  def classifyCosineSimilarity(similarity: Double): String = {
    val classification = if (similarity >= 0 && similarity <= 0.25) {
      "A"  // Class A: [0, 0.25]
    } else if (similarity > 0.25 && similarity <= 0.5) {
      "B"  // Class B: (0.25, 0.5]
    } else if (similarity > 0.5 && similarity <= 0.75) {
      "C"  // Class C: (0.5, 0.75]
    } else {
      "D"  // Class D: (0.75, 1.0]
    }
    logger.debug(s"Classified similarity $similarity as: $classification")
    classification
  }
}