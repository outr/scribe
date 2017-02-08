package spec

import java.io.File

import scribe.formatter.Formatter
import scribe.writer.FileWriter
import scribe.{LogHandler, Logger}
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source
import scala.util.Try

class FileLoggingSpec extends WordSpec with Matchers {
  lazy val fileLogger: Logger = Logger(parentName = None)
  lazy val logFile: File = new File("logs/test.log")
  lazy val writer: FileWriter = FileWriter.flat("test")

  "File Logging" should {
    "configure logging to a temporary file" in {
      logFile.delete()
      fileLogger.addHandler(LogHandler(formatter = Formatter.simple, writer = writer))
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
    "close and release the file handle" in {
      writer.close()
    }
  }
}