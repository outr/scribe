package com.outr.scribe

object LoggingTestObject extends Logging {
  updateLogger { l =>
    l.copy(parent = None)
  }
  logger.addHandler(LogHandler(Level.Debug, writer = TestingWriter))

  def testLogger(): Unit = {
    logger.info("This is a test!")
  }
}