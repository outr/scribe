package specs

import scribe.writer.NullWriter
import scribe.Logging

class LoggingTestObject(modifier: TestingModifier) extends Logging {
  logger.orphan().withHandler(writer = NullWriter, modifiers = List(modifier)).replace()

  private val anonymous = () => {
    LoggingTestObject.this.logger.info("Anonymous logging!")
  }

  def testLogger(): Unit = {
    logger.info("This is a test!")
  }

  def testAnonymous(): Unit = {
    anonymous()
  }

  def testException(): Unit = {
    logger.info(new RuntimeException("Testing"))
  }

  def testLoggerException(): Unit = {
    logger.info("Oh no", new RuntimeException("Testing"))
  }
}
