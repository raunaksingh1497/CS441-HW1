import org.deeplearning4j.text.sentenceiterator.BaseSentenceIterator

import scala.collection.mutable.ListBuffer

class TokenizedSentenceIterator(tokenizedSentences: List[List[String]]) extends BaseSentenceIterator {
  // Store the original list of sentences to allow resetting
  private val sentences: ListBuffer[List[String]] = ListBuffer(tokenizedSentences: _*)
  private var iterator: Iterator[List[String]] = sentences.iterator
  // Return the next sentence by joining the tokens into a space-separated string
  override def nextSentence(): String = {
    if (hasNext()) iterator.next().mkString(" ") else null
  }
  override def hasNext: Boolean = iterator.hasNext
  // Properly reset the iterator to the beginning of the corpus
  override def reset(): Unit = {
    iterator = sentences.iterator
  }
  // No specific finish logic needed
  override def finish(): Unit = {}
}