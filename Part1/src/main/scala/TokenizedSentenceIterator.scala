import org.deeplearning4j.text.sentenceiterator.BaseSentenceIterator
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.mutable.ListBuffer

class TokenizedSentenceIterator(tokenizedSentences: List[List[String]]) extends BaseSentenceIterator {
  // Logger for this class
  private val logger: Logger = LoggerFactory.getLogger(classOf[TokenizedSentenceIterator])

  // Store the original list of sentences to allow resetting
  private val sentences: ListBuffer[List[String]] = ListBuffer(tokenizedSentences: _*)
  private var iterator: Iterator[List[String]] = sentences.iterator

  // Return the next sentence by joining the tokens into a space-separated string
  override def nextSentence(): String = {
    if (hasNext) {
      val nextSentence = iterator.next().mkString(" ")
      logger.debug(s"Next sentence retrieved: $nextSentence") // Log the retrieved sentence
      nextSentence
    } else {
      logger.warn("No more sentences to retrieve.") // Log a warning when no more sentences are available
      null
    }
  }

  override def hasNext: Boolean = iterator.hasNext

  // Properly reset the iterator to the beginning of the corpus
  override def reset(): Unit = {
    iterator = sentences.iterator
    logger.info("Iterator has been reset to the beginning of the corpus.") // Log reset action
  }

  // No specific finish logic needed
  override def finish(): Unit = {
    logger.info("Finished processing all sentences.") // Log finish action
  }
}