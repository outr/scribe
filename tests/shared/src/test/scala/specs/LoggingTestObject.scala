package specs

import scribe.writer.NullWriter
import scribe.{Level, LogHandler, Logging}

class LoggingTestObject(modifier: TestingModifier) extends Logging {
  update(_.orphan().withHandler(LogHandler.default.withMinimumLevel(Level.Debug).withModifier(modifier).withWriter(NullWriter)))

  def testLogger(): Unit = {
    logger.info("This is a test!")
  }

  def testException(): Unit = {
    logger.info(new RuntimeException("Testing"))
  }

  def testLoggerException(): Unit = {
    logger.info("Oh no", new RuntimeException("Testing"))
  }
}
