package specs

import scribe._
import scribe.writer.CacheWriter

class LoggingTestObject(writer: CacheWriter) extends Logging {
  logger.orphan().withHandler(writer = writer).replace()

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