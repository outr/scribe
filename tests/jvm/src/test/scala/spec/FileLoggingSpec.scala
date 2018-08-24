package spec

import java.nio.file.{Files, Path, Paths}
import java.text.SimpleDateFormat

import org.scalatest.{Matchers, WordSpec}
import scribe.Logger
import scribe.format.Formatter
import scribe.util.Time
import scribe.writer.FileWriter
import scribe.writer.action._
import scribe.writer.file.LogPath

import scala.io.Source
import scala.collection.JavaConverters._

class FileLoggingSpec extends WordSpec with Matchers {
  private var logger: Logger = Logger.empty.orphan()
  lazy val logFile: Path = Paths.get("logs/test.log")

  private var timeStamp: Long = new SimpleDateFormat("yyyy-MM-dd").parse("2018-01-01").getTime

  private def setWriter(writer: FileWriter): Unit = {
    logger = logger.clearHandlers().withHandler(formatter = Formatter.simple, writer = writer)
  }

  "File Logging" should {
    "setup" in {
      Time.function = () => timeStamp
    }
    "configure logging to a temporary file" in {
      Files.newDirectoryStream(Paths.get("logs")).iterator().asScala.foreach { path =>
        Files.delete(path)
      }
      setWriter(FileWriter().nio.path(_ => logFile))
    }
    "log to the file" in {
      logger.info("Testing File Logger")
    }
    "verify the file was logged to" in {
      Files.exists(logFile) should be(true)
      Files.lines(logFile).iterator().asScala.toList should be(List("Testing File Logger"))
    }
    "configure date formatted log files" in {
      setWriter(FileWriter().path(LogPath.daily()))
    }
    "log to date formatted file" in {
      logger.info("Testing date formatted file")
    }
    "verify the date formatted file was logged to" in {
      val path = Paths.get("logs/app-2018-01-01.log")
      Files.exists(path) should be(true)
      Files.lines(path).iterator().asScala.toList should be(List("Testing date formatted file"))
      Files.lines(logFile).iterator().asScala.toList should be(List("Testing File Logger"))
    }
    "change the timeStamp and write another log record" in {
      timeStamp += 1000 * 60 * 60 * 12
      logger.info("Testing mid-day")
    }
    "verify that two records are in the date formatted file" in {
      val path = Paths.get("logs/app-2018-01-01.log")
      Files.exists(path) should be(true)
      Files.lines(path).iterator().asScala.toList should be(List("Testing date formatted file", "Testing mid-day"))
      Files.lines(logFile).iterator().asScala.toList should be(List("Testing File Logger"))
    }
    "increment timeStamp to the next day" in {
      timeStamp += 1000 * 60 * 60 * 12
      logger.info("Testing next day")
    }
    "verify that a new log file is created" in {
      val day1 = Paths.get("logs/app-2018-01-01.log")
      val day2 = Paths.get("logs/app-2018-01-02.log")
      Files.exists(day2) should be(true)
      Files.lines(day2).iterator().asScala.toList should be(List("Testing next day"))
      Files.lines(day1).iterator().asScala.toList should be(List("Testing date formatted file", "Testing mid-day"))
      Files.lines(logFile).iterator().asScala.toList should be(List("Testing File Logger"))
    }
    // TODO: testing of rolling files
    // TODO: testing of gzipping files
    // TODO: testing of max size logs
    // TODO: testing of max number of logs
    "tear down" in {
      Time.reset()
    }
  }
}