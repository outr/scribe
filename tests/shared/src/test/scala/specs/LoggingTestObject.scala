package specs

import scribe.handler.LogHandler
import scribe.writer.NullWriter
import scribe.{Level, Logging}

class LoggingTestObject(modifier: TestingModifier) extends Logging {
  import scribe.LogRecord.Stringify._

  update(_.orphan().withHandler(LogHandler(
    minimumLevel = Level.Debug,
    writer = NullWriter,
    modifiers = List(modifier)
  )))

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
