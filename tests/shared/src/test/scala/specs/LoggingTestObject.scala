package specs

import scribe.{Level, LogHandler, Logging}

class LoggingTestObject(writer: TestingWriter) extends Logging {
  logger.update {
    logger.copy(parentName = None)
  }
  logger.addHandler(LogHandler(Level.Debug, writer = writer))

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
