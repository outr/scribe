package specs

import scribe._
import org.scalatest.{Matchers, WordSpec}
import scribe.modify.LogBooster

class LoggingSpec extends WordSpec with Matchers with Logging {
  "Logging" should {
    val testingWriter = new TestingWriter
    val testObject = new LoggingTestObject(testingWriter)
    val handler = LogHandler.default.withWriter(testingWriter)

    "set up the logging" in {
      testingWriter.clear()
      update(_.orphan().withHandler(handler))
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
      update(_.withoutHandler(handler).withHandler(LogHandler.default.withWriter(testingWriter)))
      //      logger.removeHandler(handler)
      //      logger.addHandler(LogHandler(level = Level.Info, writer = testingWriter))
      logger.debug("Debug Log 2")
      testingWriter.records.length should be(2)
    }
    "boost the this logging instance" in {
      update(_.withModifier(LogBooster.multiply(2.0)))
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