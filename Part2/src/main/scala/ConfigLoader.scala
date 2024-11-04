import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader {
  private val config: Config = ConfigFactory.load() // Loads application.conf from resources

  // Paths
  val localInputFilePath: String = if (config.hasPath("inputFilePath")) config.getString("inputFilePath") else "/Users/ronny/Desktop/CS441-HW1/Part2/src/main/resources/Input/part-r-00000"
  val s3InputPath: String = if (config.hasPath("s3InputFilePath")) config.getString("s3InputFilePath") else "s3a://default-bucket/input"
  val localModelPath: String = if (config.hasPath("localModelPath")) config.getString("localModelPath") else "/Users/ronny/Desktop/CS441-HW1/Part2/trained_model.zip"
  val s3ModelPath: String = if (config.hasPath("s3ModelPath")) config.getString("s3ModelPath") else "s3a://default-bucket/model.zip"
  val localLogPath: String = if (config.hasPath("localLogPath")) config.getString("localLogPath") else "/Users/ronny/Desktop/CS441-HW1/Part2/src/main/resources/output/statistic.txt"
  val s3LogPath: String = if (config.hasPath("s3LogPath")) config.getString("s3LogPath") else "s3a://default-bucket/log.txt"
  val s3Path: String = if (config.hasPath("s3Path")) config.getString("s3Path") else "s3a://default-bucket/"

  // S3 Credentials and endpoint
  val s3AccessKey: String = if (config.hasPath("s3AccessKey")) config.getString("s3AccessKey") else "defaultAccessKey"
  val s3SecretKey: String = if (config.hasPath("s3SecretKey")) config.getString("s3SecretKey") else "defaultSecretKey"
  val s3Endpoint: String = if (config.hasPath("s3Endpoint")) config.getString("s3Endpoint") else "s3.amazonaws.com"

  // Sliding window parameters
  val windowSize: Int = if (config.hasPath("windowSize")) config.getInt("windowSize") else 10
  val overlap: Int = if (config.hasPath("overlap")) config.getInt("overlap") else 5

  // Spark settings
  val sparkAppName: String = if (config.hasPath("spark.appName")) config.getString("spark.appName") else "DefaultSparkApp"
  val sparkMaster: String = if (config.hasPath("spark.master")) config.getString("spark.master") else "local[*]"
  val sparkS3Master: String = if (config.hasPath("spark.s3master")) config.getString("spark.s3master") else "yarn"
  val sparkLocalDir: String = if (config.hasPath("spark.localDir")) config.getString("spark.localDir") else "/tmp/spark"
  val sparkExecutorMemory: String = if (config.hasPath("spark.executor.memory")) config.getString("spark.executor.memory") else "2g"
  val sparkDriverMemory: String = if (config.hasPath("spark.driver.memory")) config.getString("spark.driver.memory") else "2g"

  // Test Input
  val testInput: String = if (config.hasPath("testInput")) config.getString("testInput") else "/Users/ronny/Desktop/CS441-HW1/Part2/src/test/scala/resources/Input/input.txt"
}
