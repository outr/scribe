package com.outr.scribe

import java.io.File

import com.outr.scribe.formatter.Formatter
import com.outr.scribe.writer.FileWriter
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

class LoggingSpec extends WordSpec with Matchers with Logging {
  updateLogger { l =>
    l.copy(parent = None)
  }
  val handler = LogHandler(level = Level.Debug, writer = TestingWriter)
  logger.addHandler(handler)

  lazy val fileLogger = Logger("fileLogger", parent = None)
  lazy val logFile = new File("logs/test.log")

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
      updateLogger(_.copy(multiplier = 2.0))
      logger.debug("Debug Log 3")
      TestingWriter.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.Root.error("Error Log 1")
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
    "configure logging to a temporary file" in {
      logFile.delete()
      fileLogger.addHandler(LogHandler(formatter = Formatter.Simple, writer = FileWriter.Flat("test")))
    }
    "log to the file" in {
      fileLogger.info("Testing File Logger")
    }
    "verify the file was logged to" in {
      logFile.exists() should be(true)
      val source = Source.fromFile(logFile)
      try {
        source.mkString.trim should equal("Testing File Logger")
      } finally {
        source.close()
        logFile.delete()
      }
    }
  }
}