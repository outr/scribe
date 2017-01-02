package com.outr.scribe

import org.scalatest.{Matchers, WordSpec}

class LoggingSpec extends WordSpec with Matchers with Logging {
  logger.update {
    logger.copy(parentName = None)
  }
  val handler = LogHandler(level = Level.Debug, writer = TestingWriter)
  logger.addHandler(handler)

  "Logging" should {
    "have no logged entries yet" in {
      TestingWriter.records.length should be(0)
    }
    "log a single entry after info log" in {
      logger.info("Info Log")
      TestingWriter.records.length should be(1)
    }
    "log a second entry after debug log" in {
      logger.debug("Debug Log")
      TestingWriter.records.length should be(2)
    }
    "ignore the third entry after reconfiguring without debug logging" in {
      logger.removeHandler(handler)
      logger.addHandler(LogHandler(level = Level.Info, writer = TestingWriter))
      logger.debug("Debug Log 2")
      TestingWriter.records.length should be(2)
    }
    "boost the this logging instance" in {
      logger.update {
        logger.copy(multiplier = 2.0)
      }
      logger.debug("Debug Log 3")
      TestingWriter.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.root.error("Error Log 1")
      TestingWriter.records.length should be(3)
    }
    "write a detailed log message" in {
      val lineNumber = 10
      TestingWriter.clear()
      LoggingTestObject.testLogger()
      TestingWriter.records.length should be(1)
      TestingWriter.records.head.methodName should be(Some("testLogger"))
      TestingWriter.records.head.lineNumber should be(lineNumber)
    }
    "write an exception" in {
      val lineNumber = 14
      TestingWriter.clear()
      LoggingTestObject.testException()
      TestingWriter.records.length should be(1)
      TestingWriter.records.head.methodName should be(Some("testException"))
      TestingWriter.records.head.lineNumber should be(lineNumber)
      TestingWriter.records.head.message should startWith("java.lang.RuntimeException: Testing")
    }
  }
}