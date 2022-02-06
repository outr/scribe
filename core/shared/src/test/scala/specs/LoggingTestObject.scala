package specs

import scribe._

class LoggingTestObject(handler: TestingHandler) extends Logging {
  logger.orphan().withHandler(handler).replace()

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