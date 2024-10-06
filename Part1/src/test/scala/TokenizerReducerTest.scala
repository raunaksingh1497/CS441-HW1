import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.Logger
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

class TokenizerReducerTest extends AnyFunSuite with MockitoSugar with BeforeAndAfter {

  var mockContext: Reducer[Text, Text, Text, Text]#Context = _
  var mockLogger: Logger = _
  var reducer: TokenizerReducer = _

  before {
    // Create mock instances
    mockContext = mock[Reducer[Text, Text, Text, Text]#Context]
    mockLogger = mock[Logger]

    // Create an instance of the reducer
    reducer = new TokenizerReducer

    // Inject mocked logger
    reducer.logger = mockLogger
  }

  test("Test reduce function with multiple tokenized sentences") {
    // Arrange
    val key = new Text("sentence1")
    val values = List(new Text("1 2 3"), new Text("4 5 6")).asJava

    // Act
    reducer.reduce(key, values, mockContext)

    // Assert
    verify(mockLogger).info(s"Reducing key: ${key.toString}")
    verify(mockLogger).debug(s"Tokenized sentence added: List(1, 2, 3)")
    verify(mockLogger).debug(s"Tokenized sentence added: List(4, 5, 6)")
    verify(mockLogger).info(s"Total tokenized sentences collected for key '${key.toString}': 2")

    // Verify that the internal state has two tokenized sentences
    assert(reducer.allTokenizedSentences == List(List(1, 2, 3), List(4, 5, 6)))
  }

  test("Test cleanup phase and JSON output") {
    // Arrange
    reducer.allTokenizedSentences = List(List(1, 2, 3), List(4, 5, 6))

    // Act
    reducer.cleanup(mockContext)

    // Capture the expected JSON output
    val expectedJson = Json.stringify(Json.toJson(reducer.allTokenizedSentences))

    // Assert
    verify(mockContext).write(null, new Text(expectedJson))
    verify(mockLogger).info(s"Final JSON output written for key: ${expectedJson.take(100)}...")
  }

  test("Test reduce function with no values") {
    // Arrange
    val key = new Text("sentence2")
    val values = ListBuffer[Text]().asJava // No values

    // Act
    reducer.reduce(key, values, mockContext)

    // Assert
    verify(mockLogger).info(s"Reducing key: ${key.toString}")
    verify(mockLogger).info(s"Total tokenized sentences collected for key '${key.toString}': 0")

    // Verify that no tokenized sentences were added
    assert(reducer.allTokenizedSentences.isEmpty)
  }
}