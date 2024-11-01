import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork

object LLMInference {

  /**
   * Loads the pre-trained model and predicts the next embedding given an input array of embeddings.
   *
   * @param modelPath Path to the pre-trained model.
   * @param inputEmbeddings Array[Double] representing the input embedding sequence.
   * @return Array[Double] representing the predicted next embedding.
   */
  def predictNextEmbedding(modelPath: String, inputEmbeddings: Array[Array[Double]]): Array[Double] = {
    // Load the pre-trained model
    val model = ModelSerializer.restoreMultiLayerNetwork(modelPath)

    val windowSize = 15 // Set to match the model's expected window size
    val embeddingSize = 10 // Set to match the embedding size

    // Pad or truncate inputEmbeddings to ensure it has exactly 15 timesteps
    val paddedEmbeddings =
      if (inputEmbeddings.length < windowSize) {
        inputEmbeddings ++ Array.fill(windowSize - inputEmbeddings.length)(Array.fill(embeddingSize)(0.0))
      } else if (inputEmbeddings.length > windowSize) {
        inputEmbeddings.take(windowSize)
      } else {
        inputEmbeddings
      }

    // Convert to INDArray with shape (1, windowSize, embeddingSize)
    val inputINDArray = Nd4j.create(paddedEmbeddings.flatten).reshape(1, windowSize, embeddingSize)

    // Perform prediction
    val outputINDArray: INDArray = model.output(inputINDArray)

    // Extract the output as a 1D array (flatten the output or access via indices)
    outputINDArray.get(NDArrayIndex.point(0), NDArrayIndex.point(0), NDArrayIndex.all()).toDoubleVector
  }
}