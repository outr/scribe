package spec

import java.io.File

import com.outr.scribe.formatter.Formatter
import com.outr.scribe.writer.FileWriter
import com.outr.scribe.{LogHandler, Logger}
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

class FileLoggingSpec extends WordSpec with Matchers {
  lazy val fileLogger = Logger("fileLogger", parent = None)
  lazy val logFile = new File("logs/test.log")

  "File Logging" should {
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