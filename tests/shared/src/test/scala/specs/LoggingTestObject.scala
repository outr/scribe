package specs

import scribe.handler.LogHandler
import scribe.writer.NullWriter
import scribe.{Level, Logging}

class LoggingTestObject(modifier: TestingModifier) extends Logging {

  update(_.orphan().withHandler(LogHandler(
    writer = NullWriter,
    modifiers = List(modifier)
  )))

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
