package com.outr.scribe

object LoggingTestObject extends Logging {
  logger.update {
    logger.copy(parentName = None)
  }
  logger.addHandler(LogHandler(Level.Debug, writer = TestingWriter))

  def testLogger(): Unit = {
    logger.info("This is a test!")
  }

  def testException(): Unit = {
    logger.info(new RuntimeException("Testing"))
  }
}