import org.apache.spark.sql.SparkSession
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.apache.spark.SparkContext
import org.deeplearning4j.nn.conf.{GradientNormalization, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.evaluation.regression.RegressionEvaluation
import org.apache.spark.api.java.JavaRDD
import org.apache.hadoop.fs.{FileSystem, Path}

import java.io.{BufferedWriter, OutputStreamWriter}
import java.net.URI
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory

object SparkModelTraining {
  private val logger = LoggerFactory.getLogger(getClass)

  def trainWithTensor(spark: SparkSession, slidingWindowsTensor: INDArray): Unit = {
    val sc = spark.sparkContext
    val hadoopConfig = sc.hadoopConfiguration

    logger.info("Starting model training with Spark and DL4J.")

    // Define paths for model and statistics file on S3
    val modelPath = ConfigLoader.s3ModelPath
    val localModelPath = ConfigLoader.localModelPath
    val statsPath = new Path(ConfigLoader.s3LogPath)
    val localStatsPath = new Path(ConfigLoader.localLogPath)
    val modelOutputPath = new Path(localModelPath)

    val fs = FileSystem.get(new URI(localModelPath), hadoopConfig)
    val writer = new BufferedWriter(new OutputStreamWriter(fs.create(localStatsPath, true)))
    writer.write("Training statistics:\n")
    writer.write(s"Sliding Windows Tensor Shape: ${slidingWindowsTensor.shape.mkString(", ")}\n")

    logger.debug(s"Model output path set to: $localModelPath")
    logger.debug(s"Statistics log path set to: $localStatsPath")

    // Model setup
    val model = createModel(slidingWindowsTensor)
    val trainingMaster = new ParameterAveragingTrainingMaster.Builder(32)
      .averagingFrequency(5)
      .batchSizePerWorker(32)
      .workerPrefetchNumBatches(2)
      .build()
    val sparkModel = new SparkDl4jMultiLayer(sc, model, trainingMaster)

    logger.info("Model and training master initialized.")

    // Prepare training and validation data
    val allDataRDD: JavaRDD[DataSet] = createTrainingDataFromTensor(sc, slidingWindowsTensor)
    val Array(trainingData, validationData) = allDataRDD.randomSplit(Array(0.8, 0.2))
    logger.info(s"Data split into training (${trainingData.count()} samples) and validation (${validationData.count()} samples).")

    val numEpochs = 5
    val startTime = System.currentTimeMillis()
    val initialLearningRate = 0.01
    val decayRate = 0.01

    logger.info(s"Starting training for $numEpochs epochs.")

    // Training loop with gradient norm logging
    for (epoch <- 0 until numEpochs) {
      val epochStartTime = System.currentTimeMillis
      logger.debug(s"Starting epoch ${epoch + 1}.")

      try {
        sparkModel.fit(trainingData)
      } catch {
        case e: Exception =>
          logger.error(s"Error during training in epoch ${epoch + 1}: ${e.getMessage}", e)
          throw e
      }

      val currentLearningRate = initialLearningRate / (1 + decayRate * epoch)
      model.setLearningRate(currentLearningRate)
      logger.debug(s"Learning rate for epoch ${epoch + 1} set to $currentLearningRate.")

      // Capture and log gradients after epoch
      val gradientNorm = captureGradientNormsLocally(model, trainingData)
      writer.write(s"Completed epoch ${epoch + 1}, stats for it: \n")
      writer.write(s"Gradient L2 Norm: $gradientNorm\n")
      logger.info(s"Epoch ${epoch + 1} completed. Gradient L2 Norm: $gradientNorm")

      // Evaluate model on validation data and log results
      val (mse, mae, rmse, accuracy) = evaluateModel(sparkModel, validationData)
      writer.write(s"Epoch ${epoch + 1} - MSE: $mse, MAE: $mae, RMSE: $rmse, accuracy: $accuracy\n")
      logger.info(s"Validation results for epoch ${epoch + 1} - MSE: $mse, MAE: $mae, RMSE: $rmse, Accuracy: $accuracy")

      val epochEndTime = System.currentTimeMillis
      writer.write(s"Learning Rate: $currentLearningRate\n")
      writer.write(s"Epoch time: ${epochEndTime - epochStartTime} ms\n")
      writer.write(s"Completed epoch ${epoch + 1}\n")
    }

    val endTime = System.currentTimeMillis()
    writer.write(s"Total training time: ${endTime - startTime} ms\n")
    writer.flush()
    writer.close()
    logger.info(s"Training completed in ${endTime - startTime} ms.")

    // Save model to S3
    try {
      ModelSerializer.writeModel(model, fs.create(modelOutputPath), true)
      logger.info("Model saved to S3 successfully.")
    } catch {
      case e: Exception =>
        logger.error(s"Error saving model to S3: ${e.getMessage}", e)
        throw e
    }
  }

  def createModel(tensor: INDArray): MultiLayerNetwork = {
    logger.debug("Creating LSTM model configuration.")
    val conf = new NeuralNetConfiguration.Builder()
      .seed(123)
      .updater(new Adam(0.001))
      .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
      .gradientNormalizationThreshold(1.0)
      .list()
      .layer(0, new LSTM.Builder().nIn(tensor.size(1).toInt).nOut(64).activation(Activation.TANH).build())
      .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .activation(Activation.IDENTITY)
        .nIn(64)
        .nOut(1)
        .build())
      .setInputType(InputType.recurrent(tensor.size(1).toInt))
      .build()

    val model = new MultiLayerNetwork(conf)
    model.init()
    logger.info("Model created and initialized.")
    model
  }

  def createTrainingDataFromTensor(sc: SparkContext, tensor: INDArray): JavaRDD[DataSet] = {
    logger.debug("Creating training data from sliding windows tensor.")
    val numWindows = tensor.size(0).toInt
    val windowSize = tensor.size(1).toInt
    val embeddingSize = tensor.size(2).toInt
    val dataSets = new java.util.ArrayList[DataSet]()

    (0 until numWindows).foreach { i =>
      val featureArray = tensor.get(org.nd4j.linalg.indexing.NDArrayIndex.point(i), org.nd4j.linalg.indexing.NDArrayIndex.all(), org.nd4j.linalg.indexing.NDArrayIndex.all())
      val reshapedFeature = featureArray.reshape(1, windowSize, embeddingSize)
      val labelArray = featureArray.get(org.nd4j.linalg.indexing.NDArrayIndex.point(windowSize - 1), org.nd4j.linalg.indexing.NDArrayIndex.all())
      val reshapedLabel = labelArray.reshape(1, 1, embeddingSize)
      dataSets.add(new DataSet(reshapedFeature, reshapedLabel))
    }

    logger.info(s"Created ${dataSets.size()} training data samples from tensor.")
    sc.parallelize(dataSets.asScala)
  }

  def captureGradientNormsLocally(model: MultiLayerNetwork, trainingData: JavaRDD[DataSet]): Double = {
    logger.debug("Capturing gradient norms locally.")
    val localData = trainingData.collect().asScala.toList
    val dataSetIterator = new org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator(localData.asJava, 16)
    val gradientNorms = ArrayBuffer[Double]()

    while (dataSetIterator.hasNext) {
      val dataSet = dataSetIterator.next()
      model.setInput(dataSet.getFeatures)
      model.setLabels(dataSet.getLabels)
      model.computeGradientAndScore()
      val gradients = model.gradient().gradient()
      gradientNorms.append(gradients.norm2Number().doubleValue())
    }
    val avgGradientNorm = gradientNorms.sum / gradientNorms.length
    logger.debug(s"Average Gradient Norm: $avgGradientNorm")
    avgGradientNorm
  }

  def evaluateModel(sparkModel: SparkDl4jMultiLayer, validationData: JavaRDD[DataSet]): (Double, Double, Double, Double) = {
    logger.info("Evaluating model on validation data.")
    val validationDataList = validationData.collect().asScala
    val localModel = sparkModel.getNetwork
    val eval = new RegressionEvaluation()

    validationDataList.foreach { dataSet =>
      val features = dataSet.getFeatures
      val labels = dataSet.getLabels
      val predictions = localModel.output(features, false)
      eval.eval(labels, predictions)
    }

    val mse = eval.meanSquaredError(0)
    val mae = eval.meanAbsoluteError(0)
    val rmse = math.sqrt(mse)
    val r2 = eval.correlationR2(0)

    logger.info(f"Validation metrics - MSE: $mse%.4f, MAE: $mae%.4f, RMSE: $rmse%.4f, R^2: $r2%.4f")
    (mse, mae, rmse, r2)
  }
}