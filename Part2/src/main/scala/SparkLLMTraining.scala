import org.apache.spark.SparkConf
import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions

import java.util

object SparkLLMTraining {

  def createSparkContext(): JavaSparkContext = {
    val sparkConf = new SparkConf()
      .setAppName("DL4J-LanguageModel-Spark")
      .setMaster("local[*]")
      .set("spark.local.dir", "/Users/ronny/tmp/spark")

    new JavaSparkContext(sparkConf)
  }

  def trainWithTensor(slidingWindowsTensor: INDArray): Unit = {
    // Set up Spark context
    val sc = createSparkContext()

    val inputSize = slidingWindowsTensor.size(2).toInt // Size of embeddings
    val hiddenSize = 64
    val outputSize = 1  // Output size set to 1 as per requirement
    val windowSize = slidingWindowsTensor.size(1).toInt

    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(123)
      .updater(new Adam(0.001))
      .list()
      // LSTM layer with window size as input
      .layer(0, new LSTM.Builder()
        .nIn(windowSize)
        .nOut(hiddenSize)
        .activation(Activation.TANH)
        .build())
      // RNN output layer for regression
      .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .activation(Activation.IDENTITY)  // Identity activation function
        .nIn(hiddenSize)
        .nOut(outputSize)  // Output size, often set to 1 for regression
        .build())
      .setInputType(InputType.recurrent(windowSize))
      .build()

    val model: MultiLayerNetwork = new MultiLayerNetwork(conf)
    model.init()

    // Set the custom gradient norm logger
//    model.setListeners(new GradientNormLogger(1))

    val trainingMaster = new ParameterAveragingTrainingMaster.Builder(32)
      .averagingFrequency(5)
      .batchSizePerWorker(32)
      .workerPrefetchNumBatches(2)
      .build()

    val sparkModel = new SparkDl4jMultiLayer(sc, model, trainingMaster)

    // Prepare the tensor data for training
    val trainingDataRDD: JavaRDD[DataSet] = createTrainingDataFromTensor(sc, slidingWindowsTensor)

    // Define number of epochs
    val numEpochs = 5

    // Lists to store training loss and accuracy
    val trainingLosses = new util.ArrayList[Double]()
    val trainingAccuracies = new util.ArrayList[Double]()

    // Train the model using Spark
    for (epoch <- 0 until numEpochs) {
      sparkModel.fit(trainingDataRDD)
//      val trainingLoss = calculateLoss(sparkModel, trainingDataRDD)
//      val trainingAccuracy = calculateAccuracy(sparkModel, trainingDataRDD)

//      trainingLosses.add(trainingLoss)
//      trainingAccuracies.add(trainingAccuracy)
//
//      println(s"Completed epoch ${epoch + 1}, Training Loss: $trainingLoss, Training Accuracy: $trainingAccuracy")

      println(s"Completed epoch ${epoch + 1}")
    }
    // Specify the file where the model will be saved
    // Specify the file where the model will be saved
    val locationToSave = new java.io.File("/Users/ronny/Desktop/CS441Cloud/Scala/Spark/trained_model.zip")

    // Whether or not to save the updater (optimizer state)
    val saveUpdater = true

    // Save the trained model to the specified file
    ModelSerializer.writeModel(model, locationToSave, saveUpdater)
    sc.stop()
  }

  def createTrainingDataFromTensor(sc: JavaSparkContext, tensor: INDArray): JavaRDD[DataSet] = {
    val numWindows = tensor.size(0).toInt
    val windowSize = tensor.size(1).toInt
    val embeddingSize = tensor.size(2).toInt

    // List to hold DataSets
    val dataSets: java.util.List[DataSet] = new util.ArrayList[DataSet]()

    // Loop through each window in the tensor to create a DataSet
    (0 until numWindows).foreach { i =>
      // Extract feature array for the ith window
      val featureArray = tensor.get(NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.all())

      // Reshape feature array to (1, windowSize, embeddingSize)
      val reshapedFeature = featureArray.reshape(1, windowSize, embeddingSize)

      // Define the label. Here we take the last embedding of the window as the label
      val labelArray = featureArray.get(NDArrayIndex.point(windowSize - 1), NDArrayIndex.all()) // Shape [10]

      // Reshape the label to match the expected rank 3 shape: (1, 1, embeddingSize)
      val reshapedLabel = labelArray.reshape(1, 1, embeddingSize)

      // Create a DataSet with feature and label arrays
      dataSets.add(new DataSet(reshapedFeature, reshapedLabel))
    }

    // Parallelize DataSets as an RDD
    sc.parallelize(dataSets)
  }


//  class GradientNormLogger(listenerFrequency: Int) extends BaseTrainingListener {
//    override def epochDone(model: Model, epoch: Int): Unit = {
//      // Get the gradients from the model
//      val gradient: INDArray = model.getGradient().gradient()
//
//      // Compute the L2 norm of the gradients
//      val l2Norm = gradient.norm2Number().doubleValue()
//      println(s"Epoch: $epoch, Gradient Norm: $l2Norm")
//    }
//  }
}