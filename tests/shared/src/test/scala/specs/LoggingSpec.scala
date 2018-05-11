package specs

import org.scalatest.{Matchers, WordSpec}
import scribe._
import scribe.handler.LogHandler
import scribe.modify.{LevelFilter, LogBooster}
import scribe.writer.{NullWriter, Writer}
import scribe.format._

import scala.collection.mutable.ListBuffer

class LoggingSpec extends WordSpec with Matchers with Logging {
  val expectedTestFileName = "tests/shared/src/test/scala/specs/LoggingTestObject.scala"

  "Logging" should {
    val testingModifier = new TestingModifier
    val testObject = new LoggingTestObject(testingModifier)
    val handler = LogHandler(writer = NullWriter).withModifier(testingModifier)

    "set up the logging" in {
      testingModifier.clear()
      update(_.orphan().withHandler(handler))
    }
    "have no logged entries yet" in {
      testingModifier.records.length should be(0)
    }
    "log a single entry after info log" in {
      logger.info("Info Log")
      testingModifier.records.length should be(1)
    }
    "log a second entry after debug log" in {
      logger.debug("Debug Log")
      testingModifier.records.length should be(2)
    }
    "ignore the third entry after reconfiguring without debug logging" in {
      update(_
        .withoutHandler(handler)
        .withHandler(writer = NullWriter)
        .withModifier(LevelFilter >= Level.Info)
        .withModifier(testingModifier)
      )
      testingModifier.records.length should be(2)
      logger.debug("Debug Log 2")
      testingModifier.records.length should be(2)
    }
    "boost the this logging instance" in {
      update(_.withModifier(LogBooster.multiply(2.0, Priority.Critical)))
      logger.debug("Debug Log 3")
      testingModifier.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.root.error("Error Log 1")
      testingModifier.records.length should be(3)
    }
    "log using 's' interpolation" in {
      val message = "Wahoo!"
      logger.info(s"It works! $message")
    }
    "log using 'f' interpolation" in {
      val d = 12.3456
      logger.info(f"It works! $d%.0f")
    }
    "write a detailed log message" in {
      val lineNumber = Some(19)
      testingModifier.clear()
      testObject.testLogger()
      testingModifier.records.length should be(1)
      testingModifier.records.head.methodName should be(Some("testLogger"))
      testingModifier.records.head.className should be("specs.LoggingTestObject")
      testingModifier.records.head.lineNumber should be(lineNumber)
      testingModifier.records.head.fileName should endWith(expectedTestFileName)
    }
    "write a log message with an anonymous function" in {
      val lineNumber = Some(15)
      testingModifier.clear()
      testObject.testAnonymous()
      testingModifier.records.length should be(1)
      testingModifier.records.head.methodName should be(None)
      testingModifier.records.head.className should be("specs.LoggingTestObject.anonymous")
      testingModifier.records.head.lineNumber should be(lineNumber)
      testingModifier.records.head.fileName should endWith(expectedTestFileName)
    }
    "write an exception" in {
      val lineNumber = Some(27)
      testingModifier.clear()
      testObject.testException()
      testingModifier.records.length should be(1)
      testingModifier.records.head.methodName should be(Some("testException"))
      testingModifier.records.head.className should be("specs.LoggingTestObject")
      testingModifier.records.head.lineNumber should be(lineNumber)
      testingModifier.records.head.message should startWith("java.lang.RuntimeException: Testing")
      testingModifier.records.head.fileName should endWith(expectedTestFileName)
    }
    "utilize MDC logging" in {
      val logs = ListBuffer.empty[String]
      val logger = Logger.empty.withHandler(
        formatter = LoggingSpec.mdcFormatter,
        writer = new Writer {
          override def write[M](record: LogRecord[M], output: String): Unit = logs += output
        }
      )

      logger.info("A")
      MDC("test1") = "First"
      MDC("test2") = "Second"
      logger.info("B")
      MDC.remove("test1")
      logger.info("D")
      MDC.clear()
      logger.info("E")

      var pos = 0
      logs(pos) should be("null, null - A")
      pos += 1
      logs(pos) should be("First, Second - B")
      pos += 1
      logs(pos) should be("null, Second - D")
      pos += 1
      logs(pos) should be("null, null - E")
    }
  }
}

object LoggingSpec {
  val mdcFormatter: Formatter = formatter"${mdc("test1")}, ${mdc("test2")} - $message"
}
