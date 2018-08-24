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

  private def setDate(date: String): Unit = {
    timeStamp = new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime
  }

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
    "configure rolling files" in {
      setDate("2018-01-01")
      setWriter(FileWriter().path(_ => Paths.get("logs/rolling.log")).rolling(LogPath.daily("rolling"), checkRate = 0L))
    }
    "log a record to the rolling file" in {
      logger.info("Rolling 1")
    }
    "verify rolling log 1" in {
      val path = Paths.get("logs/rolling.log")
      Files.exists(path)
      Files.lines(path).iterator().asScala.toList should be(List("Rolling 1"))
    }
    "increment date and roll file" in {
      setDate("2018-01-02")
      logger.info("Rolling 2")
    }
    "verify rolling log 2" in {
      val path = Paths.get("logs/rolling.log")
      val rolled = Paths.get("logs/rolling-2018-01-01.log")
      Files.exists(path) should be(true)
      Files.exists(rolled) should be(true)
      Files.lines(path).iterator().asScala.toList should be(List("Rolling 2"))
      Files.lines(rolled).iterator().asScala.toList should be(List("Rolling 1"))
    }
    "increment date and roll file again" in {
      setDate("2018-01-03")
      logger.info("Rolling 3")
    }
    "verify rolling log 3" in {
      val path = Paths.get("logs/rolling.log")
      val rolled1 = Paths.get("logs/rolling-2018-01-01.log")
      val rolled2 = Paths.get("logs/rolling-2018-01-02.log")
      Files.exists(path) should be(true)
      Files.exists(rolled1) should be(true)
      Files.exists(rolled2) should be(true)
      Files.lines(path).iterator().asScala.toList should be(List("Rolling 3"))
      Files.lines(rolled1).iterator().asScala.toList should be(List("Rolling 1"))
      Files.lines(rolled2).iterator().asScala.toList should be(List("Rolling 2"))
    }
    // TODO: testing of gzipping files
    // TODO: testing of max size logs
    // TODO: testing of max number of logs
    "tear down" in {
      Time.reset()
    }
  }
}