import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.Logger
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import scala.jdk.CollectionConverters._

import java.net.URI
import java.io.File

class Word2VecReducerTest extends AnyFunSuite with MockitoSugar with BeforeAndAfter {

  var reducer: Word2VecReducer = _
  var mockContext: Reducer[Text, Text, Text, Text]#Context = _
  var mockFileSystem: FileSystem = _
  var mockWord2Vec: Word2Vec = _
  var mockLogger: Logger = _

  before {
    // Create mocks
    reducer = new Word2VecReducer
    mockContext = mock[Reducer[Text, Text, Text, Text]#Context]
    mockFileSystem = mock[FileSystem]
    mockWord2Vec = mock[Word2Vec]
    mockLogger = mock[Logger]

    // Inject mocked logger and word2Vec model into the reducer
    reducer.logger = mockLogger
    reducer.word2Vec = mockWord2Vec

    // Mock Hadoop Configuration and FileSystem methods
    when(mockContext.getConfiguration).thenReturn(new org.apache.hadoop.conf.Configuration())
    when(FileSystem.get(any[URI], any())).thenReturn(mockFileSystem)
  }

  test("Test Word2Vec model setup") {
    // Arrange
    val localModelPath = new File("/path/to/local/word2vec_model.bin")
    when(mockFileSystem.copyToLocalFile(anyBoolean(), any(), any(), anyBoolean())).thenReturn(())

    // Act
    reducer.setupModel(mockContext)

    // Assert
    verify(mockLogger).info(contains("Model successfully downloaded"))
    verify(mockLogger).info("Word2Vec model loaded successfully")
    assert(reducer.word2Vec != null) // Ensure model is loaded
  }

  test("Test reduce function with valid tokens") {
    // Arrange
    val key = new Text("sentence")
    val values = List(new Text("[1, 2, 3]")).asJava
    when(mockWord2Vec.getWordVector("1")).thenReturn(Array(0.1, 0.2, 0.3))
    when(mockWord2Vec.getWordVector("2")).thenReturn(Array(0.4, 0.5, 0.6))
    when(mockWord2Vec.getWordVector("3")).thenReturn(Array(0.7, 0.8, 0.9))

    // Act
    reducer.reduce(key, values, mockContext)

    // Assert
    verify(mockLogger).info("Writing token: 1 with embedding: 0.1,0.2,0.3")
    verify(mockLogger).info("Writing token: 2 with embedding: 0.4,0.5,0.6")
    verify(mockLogger).info("Writing token: 3 with embedding: 0.7,0.8,0.9")
    verify(mockContext).write(new Text("1"), new Text("0.1,0.2,0.3"))
    verify(mockContext).write(new Text("2"), new Text("0.4,0.5​⬤