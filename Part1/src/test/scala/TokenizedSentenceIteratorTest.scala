import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfter
import org.slf4j.{Logger, LoggerFactory}

class TokenizedSentenceIteratorTest extends AnyFunSuite with BeforeAndAfter {

  var iterator: TokenizedSentenceIterator = _
  private val logger: Logger = LoggerFactory.getLogger(classOf[TokenizedSentenceIteratorTest])

  before {
    // Sample tokenized sentences for testing
    val tokenizedSentences = List(
      List("This", "is", "a", "sentence"),
      List("Another", "sentence", "with", "tokens")
    )
    iterator = new TokenizedSentenceIterator(tokenizedSentences)
  }

  test("Test nextSentence retrieves the correct sentence") {
    logger.info("Testing nextSentence() for correct sentence retrieval.")

    // First sentence
    val sentence1 = iterator.nextSentence()
    assert(sentence1 == "This is a sentence", "First sentence should match")

    // Second sentence
    val sentence2 = iterator.nextSentence()
    assert(sentence2 == "Another sentence with tokens", "Second sentence should match")
  }

  test("Test hasNext returns true when more sentences exist") {
    logger.info("Testing hasNext() when more sentences are present.")

    assert(iterator.hasNext, "Iterator should have more sentences initially")
    iterator.nextSentence()
    assert(iterator.hasNext, "Iterator should still have more sentences after one call")
  }

  test("Test hasNext returns false when no sentences remain") {
    logger.info("Testing hasNext() when no more sentences remain.")

    iterator.nextSentence()
    iterator.nextSentence()
    assert(!iterator.hasNext, "Iterator should have no more sentences")
  }

  test("Test nextSentence returns null when no sentences remain") {
    logger.info("Testing nextSentence() returns null when no sentences remain.")

    iterator.nextSentence() // First sentence
    iterator.nextSentence() // Second sentence
    val sentence3 = iterator.nextSentence()
    assert(sentence3 == null, "Should return null when no more sentences are available")
  }

  test("Test reset returns the iterator to the beginning") {
    logger.info("Testing reset() functionality.")

    iterator.nextSentence() // First sentence
    iterator.reset() // Reset iterator

    // First sentence again after reset
    val resetSentence = iterator.nextSentence()
    assert(resetSentence == "This is a sentence", "After reset, iterator should return to the first sentence")
  }

  test("Test finish logs a completion message") {
    logger.info("Testing finish() logs completion.")

    // Simply call finish and verify that it logs (manual log inspection needed, not an assert)
    iterator.finish()
  }
}