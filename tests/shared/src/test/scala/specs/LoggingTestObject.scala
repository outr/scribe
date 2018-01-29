package specs

import scribe.modify.LevelFilter
import scribe.writer.NullWriter
import scribe.{Level, LogHandler, Logging}

class LoggingTestObject(modifier: TestingModifier) extends Logging {
  update(_.orphan().withHandler(LogHandler.default.withModifier(LevelFilter >= Level.Debug).withModifier(modifier).withWriter(NullWriter)))

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
