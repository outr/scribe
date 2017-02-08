package specs

import scribe._
import org.scalatest.{Matchers, WordSpec}

class LoggingSpec extends WordSpec with Matchers with Logging {
  "Logging" should {
    val testingWriter = new TestingWriter
    val testObject = new LoggingTestObject(testingWriter)
    val handler = LogHandler(level = Level.Debug, writer = testingWriter)

    "set up the logging" in {
      testingWriter.clear()
      logger.update {
        logger.copy(parentName = None)
      }
      logger.addHandler(handler)
    }
    "have no logged entries yet" in {
      testingWriter.records.length should be(0)
    }
    "log a single entry after info log" in {
      logger.info("Info Log")
      testingWriter.records.length should be(1)
    }
    "log a second entry after debug log" in {
      logger.debug("Debug Log")
      testingWriter.records.length should be(2)
    }
    "ignore the third entry after reconfiguring without debug logging" in {
      logger.removeHandler(handler)
      logger.addHandler(LogHandler(level = Level.Info, writer = testingWriter))
      logger.debug("Debug Log 2")
      testingWriter.records.length should be(2)
    }
    "boost the this logging instance" in {
      logger.update {
        logger.copy(multiplier = 2.0)
      }
      logger.debug("Debug Log 3")
      testingWriter.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.root.error("Error Log 1")
      testingWriter.records.length should be(3)
    }
    "write a detailed log message" in {
      val lineNumber = 12
      testingWriter.clear()
      testObject.testLogger()
      testingWriter.records.length should be(1)
      testingWriter.records.head.methodName should be(Some("testLogger"))
      testingWriter.records.head.lineNumber should be(lineNumber)
    }
    "write an exception" in {
      val lineNumber = 16
      testingWriter.clear()
      testObject.testException()
      testingWriter.records.length should be(1)
      testingWriter.records.head.methodName should be(Some("testException"))
      testingWriter.records.head.lineNumber should be(lineNumber)
      testingWriter.records.head.message should startWith("java.lang.RuntimeException: Testing")
    }
  }
}