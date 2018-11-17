package spec

import java.nio.file.{Files, Path, Paths}
import java.text.SimpleDateFormat

import org.scalatest.{Matchers, WordSpec}
import scribe.Logger
import scribe.format.Formatter
import scribe.util.Time
import scribe.writer.FileWriter
import scribe.writer.file.LogPath

import scala.collection.JavaConverters._
import scala.concurrent.duration._

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
      val directory = Paths.get("logs")
      if (Files.exists(directory)) {
        Files.newDirectoryStream(directory).iterator().asScala.foreach { path =>
          Files.delete(path)
        }
      }
      setWriter(FileWriter().autoFlush.nio.path(_ => logFile))
    }
    "log to the file" in {
      logger.info("Testing File Logger")
    }
    "verify the file was logged to" in {
      waitForExists(logFile) should be(true)
      linesFor(logFile) should be(List("Testing File Logger"))
    }
    "configure date formatted log files" in {
      setWriter(FileWriter().autoFlush.path(LogPath.daily()))
    }
    "log to date formatted file" in {
      logger.info("Testing date formatted file")
    }
    "verify the date formatted file was logged to" in {
      val path = Paths.get("logs/app-2018-01-01.log")
      waitForExists(path) should be(true)
      linesFor(path) should be(List("Testing date formatted file"))
      linesFor(logFile) should be(List("Testing File Logger"))
    }
    "change the timeStamp and write another log record" in {
      timeStamp += 1000 * 60 * 60 * 12
      logger.info("Testing mid-day")
    }
    "verify that two records are in the date formatted file" in {
      val path = Paths.get("logs/app-2018-01-01.log")
      waitForExists(path) should be(true)
      linesFor(path, linesMinimum = 2) should be(List("Testing date formatted file", "Testing mid-day"))
      linesFor(logFile) should be(List("Testing File Logger"))
    }
    "increment timeStamp to the next day" in {
      timeStamp += 1000 * 60 * 60 * 12
      logger.info("Testing next day")
    }
    "verify that a new log file is created" in {
      val day1 = Paths.get("logs/app-2018-01-01.log")
      val day2 = Paths.get("logs/app-2018-01-02.log")
      waitForExists(day2) should be(true)
      linesFor(day2) should be(List("Testing next day"))
      linesFor(day1) should be(List("Testing date formatted file", "Testing mid-day"))
      linesFor(logFile) should be(List("Testing File Logger"))
    }
    "configure rolling files" in {
      setDate("2018-01-01")
      setWriter(FileWriter().autoFlush.path(_ => Paths.get("logs/rolling.log")).rolling(LogPath.daily("rolling"), checkRate = 0.millis))
    }
    "log a record to the rolling file" in {
      logger.info("Rolling 1")
    }
    "verify rolling log 1" in {
      val path = Paths.get("logs/rolling.log")
      waitForExists(path) should be(true)
      linesFor(path) should be(List("Rolling 1"))
    }
    "increment date and roll file" in {
      setDate("2018-01-02")
      logger.info("Rolling 2")
    }
    "verify rolling log 2" in {
      val path = Paths.get("logs/rolling.log")
      val rolled = Paths.get("logs/rolling-2018-01-01.log")
      waitForExists(path) should be(true)
      waitForExists(rolled) should be(true)
      linesFor(path) should be(List("Rolling 2"))
      linesFor(rolled) should be(List("Rolling 1"))
    }
    "increment date and roll file again" in {
      setDate("2018-01-03")
      logger.info("Rolling 3")
    }
    "verify rolling log 3" in {
      val path = Paths.get("logs/rolling.log")
      val rolled1 = Paths.get("logs/rolling-2018-01-01.log")
      val rolled2 = Paths.get("logs/rolling-2018-01-02.log")
      waitForExists(path) should be(true)
      waitForExists(rolled1) should be(true)
      waitForExists(rolled2) should be(true)
      linesFor(path) should be(List("Rolling 3"))
      linesFor(rolled1) should be(List("Rolling 1"))
      linesFor(rolled2) should be(List("Rolling 2"))
    }
    "configure daily path with gzipping" in {
      setDate("2018-01-01")
      setWriter(FileWriter().autoFlush.path(LogPath.daily("gzip"), gzip = true, checkRate = 0.millis))
    }
    "log a record pre gzip" in {
      logger.info("Gzip 1")
    }
    "verify gzipping log 1" in {
      val path = Paths.get("logs/gzip-2018-01-01.log")
      waitForExists(path) should be(true)
      linesFor(path) should be(List("Gzip 1"))
    }
    "modify date to create gzip" in {
      setDate("2018-01-02")
      logger.info("Gzip 2")
    }
    "verify gzipping log 2" in {
      val path = Paths.get("logs/gzip-2018-01-02.log")
      val gzipped = Paths.get("logs/gzip-2018-01-01.log.gz")
      val unGzipped = Paths.get("logs/gzip-2018-01-01.log")
      waitForExists(path) should be(true)
      waitForExists(gzipped) should be(true)
      waitForExists(unGzipped) should be(false)
      linesFor(path) should be(List("Gzip 2"))
    }
    "configure maximum sized log files" in {
      setWriter(FileWriter()
        .autoFlush
        .path(LogPath.simple("max.sized.log"))
        .maxSize(1L, checkRate = 0.millis)
      )
    }
    "write three log records across three log files" in {
      logger.info("Record 1")
      logger.info("Record 2")
      logger.info("Record 3")
    }
    "verify three log files exist with the proper records" in {
      val p1 = Paths.get("logs/max.sized.log")
      val p2 = Paths.get("logs/max.sized.1.log")
      val p3 = Paths.get("logs/max.sized.2.log")
      waitForExists(p1) should be(true)
      waitForExists(p2) should be(true)
      waitForExists(p3) should be(true)
      linesFor(p1) should be(List("Record 3"))
      linesFor(p2) should be(List("Record 2"))
      linesFor(p3) should be(List("Record 1"))
    }
    "configure maximum number of log files" in {
      setWriter(FileWriter()
        .autoFlush
        .path(LogPath.simple("maxlogs.log"))
        .maxSize(1L, checkRate = 0.millis)
        .maxLogs(3, checkRate = 0.millis))
    }
    "write four log records for a maximum of three log files" in {
      logger.info("Record 1")
      logger.info("Record 2")
      logger.info("Record 3")
      logger.info("Record 4")
    }
    "verify only three log files exist" in {
      val p1 = Paths.get("logs/maxlogs.log")
      val p2 = Paths.get("logs/maxlogs.1.log")
      val p3 = Paths.get("logs/maxlogs.2.log")
//      val p4 = Paths.get("logs/maxlogs.3.log")
      waitForExists(p1) should be(true)
      waitForExists(p2) should be(true)
      waitForExists(p3) should be(true)
//      waitForDeleted(p4) should be(false)
      linesFor(p1) should be(List("Record 4"))
      linesFor(p2) should be(List("Record 3"))
      linesFor(p3) should be(List("Record 2"))
    }
    "tear down" in {
      Time.reset()
    }
  }

  private def waitForExists(path: Path, timeout: Long = 5.seconds.toMillis): Boolean = if (Files.exists(path)) {
    true
  } else if (timeout >= 0L) {
    Thread.sleep(1.second.toMillis)
    waitForExists(path, timeout - 1.second.toMillis)
  } else {
    false
  }

  private def waitForDeleted(path: Path, timeout: Long = 5.seconds.toMillis): Boolean = if (!Files.exists(path)) {
    false
  } else if (timeout >= 0L) {
    Thread.sleep(1.second.toMillis)
    waitForDeleted(path, timeout - 1.second.toMillis)
  } else {
    true
  }

  private def linesFor(path: Path, linesMinimum: Int = 1, waitForData: Long = 5.seconds.toMillis): List[String] = {
    val lines = Files.lines(path).iterator().asScala.toList
    if (lines.nonEmpty && lines.size >= linesMinimum) {
      lines
    } else if (waitForData > 0L) {
      Thread.sleep(1.second.toMillis)
      linesFor(path, linesMinimum, waitForData - 1.second.toMillis)
    } else {
      lines
    }
  }
}