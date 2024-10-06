import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar
import org.apache.log4j.Logger
import scala.collection.mutable.ListBuffer
import java.lang.Iterable

class CosineSimilarityReducerTest extends AnyFunSuite with MockitoSugar with BeforeAndAfter {

  var mockContext: Reducer[Text, Text, Text, Text]#Context = _
  var mockLogger: Logger = _
  var reducer: CosineSimilarityReducer = _

  before {
    // Create mock instances
    mockContext = mock[Reducer[Text, Text, Text, Text]#Context]
    mockLogger = mock[Logger]

    // Create an instance of the reducer
    reducer = new CosineSimilarityReducer

    // Inject mocked logger
    reducer.logger = mockLogger
  }

  test("Test setup phase logging") {
    // Act
    reducer.setup(mockContext)

    // Assert
    verify(mockLogger).info("Reducer setup phase started.")
    verify(mockLogger).info("Reducer setup phase completed.")
  }

  test("Test reduce function with multiple values") {
    // Arrange
    val key = new Text("token1")
    val values = List(new Text("0.75"), new Text("0.8")).asJava

    // Act
    reducer.reduce(key, values, mockContext)

    // Assert
    verify(mockLogger).info(s"Reducing key: $key")
    verify(mockLogger).debug(s"Processing value for key $key: 0.75")
    verify(mockLogger).debug(s"Processing value for key $key: 0.8")
    verify(mockLogger).info(s"Total values processed for key $key: 2")
    verify(mockLogger).info(s"Writing result for key $key: 0.75")
    verify(mockLogger).info(s"Writing result for key $key: 0.8")
    verify(mockContext).write(key, new Text("0.75"))
    verify(mockContext).write(key, new Text("0.8"))
  }

  test("Test reduce function with no values") {
    // Arrange
    val key = new Text("token2")
    val values = ListBuffer[Text]().asJava // No values

    // Act
    reducer.reduce(key, values, mockContext)

    // Assert
    verify(mockLogger).info(s"Reducing key: $key")
    verify(mockLogger).info(s"Total values processed for key $key: 0")
    verify(mockContext, never()).write(key, any())
  }

  test("Test cleanup phase logging") {
    // Act
    reducer.cleanup(mockContext)

    // Assert
    verify(mockLogger).info("Reducer cleanup phase completed.")
  }
}