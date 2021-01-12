package spec

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.file.FileWriter
import scribe.format._
import scribe.output.format.{ASCIIOutputFormat, OutputFormat}
import scribe.util.Time
import scribe.{Level, Logger}

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.duration._
import scala.language.implicitConversions
import scribe.file._

import java.nio.file.attribute.FileTime
import java.util.Calendar
import java.util.function.Consumer
import scala.annotation.tailrec

class FileLoggingSpec extends AnyWordSpec with Matchers {
  private var logger: Logger = Logger.empty.orphan()
  lazy val logFile: Path = Paths.get("logs/test.log")

  private var timeStamp: Long = 0L

  private val DateRegex = """(\d{4})-(\d{2})-(\d{2})""".r

  setDate("2018-01-01")

  private def setDate(date: String): Unit = date match {
    case DateRegex(year, month, day) => {
      val c = Calendar.getInstance()
      c.set(year.toInt, month.toInt - 1, day.toInt, 0, 0, 0)
      timeStamp = c.getTimeInMillis
    }
  }

  private var writer: FileWriter = _

  private def setWriter(writer: FileWriter): Unit = {
    logger = logger.clearHandlers().withHandler(formatter = Formatter.simple, writer = writer)
    this.writer = writer
  }

  private def validateLogs(fileNames: String*): Assertion = {
    writer.list().map(_.getFileName.toString).toSet should be(fileNames.toSet)
  }

  "File Logging" when {
    "setup" in {
      OutputFormat.default = ASCIIOutputFormat
      Time.function = () => timeStamp
    }
    "verifying simple logging" should {
      "configure logging to a temporary file" in {
        val directory = Paths.get("logs")
        if (Files.exists(directory)) {
          Files.newDirectoryStream(directory).forEach(new Consumer[Path] {
            override def accept(path: Path): Unit = Files.delete(path)
          })
        }
        setWriter(FileWriter(logFile).flushAlways)
      }
      "log to the file" in {
        logger.info("Testing File Logger")
      }
      "verify the file was logged to" in {
        waitForExists(logFile) should be(true)
        linesFor(logFile) should be(List("Testing File Logger"))
      }
      "verify the writer lists the logged files" in {
        validateLogs("test.log")
      }
    }
    "verifying date logging" should {
      "configure date formatted log files" in {
        setWriter(FileWriter(Paths.get("logs") / ("app-" % year % "-" % month % "-" % day % ".log")).flushAlways)
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
      "verify the writer lists the logged files" in {
        validateLogs("app-2018-01-02.log", "app-2018-01-01.log")
      }
    }
    "verifying rolling logging" should {
      "configure rolling files" in {
        setDate("2018-01-01")
        setWriter(
          FileWriter("logs" / ("rolling" % rolling("-" % year % "-" % month % "-" % day) % ".log")).flushAlways
        )
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
        Files.setLastModifiedTime(writer.path, FileTime.fromMillis(Time()))
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
        Files.setLastModifiedTime(writer.path, FileTime.fromMillis(Time()))
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
      "verify the writer lists the logged files" in {
        validateLogs("rolling.log", "rolling-2018-01-01.log", "rolling-2018-01-02.log")
      }
    }
    "verifying GZIP support" should {
      "configure daily path with gzipping" in {
        setDate("2018-01-01")
        setWriter(FileWriter("logs" / ("gzip-" % year % "-" % month % "-" % day % ".log" % rollingGZIP())).flushAlways)
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
        Files.setLastModifiedTime(writer.path, FileTime.fromMillis(Time()))
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
      "verify the writer lists the logged files" in {
        validateLogs("gzip-2018-01-02.log", "gzip-2018-01-01.log.gz")
      }
    }
    // "logs" / ("max.sized" % maxSize() % ".log")
    "verifying max size log files" should {
      "configure maximum sized log files" in {
        setWriter(FileWriter("logs" / ("max.sized" % maxSize(max = 1L, separator = ".") % ".log")).flushAlways)
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
    }
    "verifying max number of log files" should {
      "configure maximum number of log files" in {
        setWriter(
          FileWriter("logs" / ("maxlogs" % maxSize(max = 1L, separator = ".") % maxLogs(3) % ".log")).flushAlways
        )
      }
      "write four log records for a maximum of three log files" in {
        logger.info("Record 1")
        Thread.sleep(1000L)
        logger.info("Record 2")
        Thread.sleep(1000L)
        logger.info("Record 3")
        Thread.sleep(1000L)
        logger.info("Record 4")
      }
      "verify only three log files exist" in {
        val p1 = Paths.get("logs/maxlogs.log")
        val p2 = Paths.get("logs/maxlogs.1.log")
        val p3 = Paths.get("logs/maxlogs.2.log")
        val p4 = Paths.get("logs/maxlogs.3.log")
        waitForExists(p1) should be(true)
        waitForExists(p2) should be(true)
        waitForExists(p3) should be(true)
        waitForDeleted(p4) should be(false)
        linesFor(p1) should be(List("Record 4"))
        linesFor(p2) should be(List("Record 3"))
        linesFor(p3) should be(List("Record 2"))
      }
    }
    "verifying corner cases" should {
      "rolling logging for an existing log file should roll properly" in {
        val path1 = Paths.get("logs", "rolling1.log")
        val path2 = Paths.get("logs", "rolling1.2018.01.01.log")
        val path3 = Paths.get("logs", "rolling1.2018.01.02.log")
        Files.deleteIfExists(path1)
        Files.deleteIfExists(path2)
        Files.deleteIfExists(path3)

        setDate("2018-01-01")

        setWriter(
          FileWriter("logs" / ("rolling1" % rolling("." % year % "." % month % "." % day) % ".log")).flushAlways
        )
        writer.path should be(path1)
        val logger = Logger
          .empty
          .orphan()
          .withHandler(
            formatter = Formatter.simple,
            writer = writer,
            minimumLevel = Some(Level.Trace)
          )

        logger.debug("Test 1")
        linesFor(path1) should be(List("Test 1"))
        linesFor(path2) should be(Nil)
        linesFor(path3) should be(Nil)

        Files.setLastModifiedTime(writer.path, FileTime.fromMillis(Time()))
        setDate("2018-01-02")
        logger.debug("Test 2")
        linesFor(path1) should be(List("Test 2"))
        linesFor(path2) should be(List("Test 1"))
        linesFor(path3) should be(Nil)

        Files.setLastModifiedTime(writer.path, FileTime.fromMillis(Time()))
        setDate("2018-01-03")
        logger.debug("Test 3")
        linesFor(path1) should be(List("Test 3"))
        linesFor(path2) should be(List("Test 1"))
        linesFor(path3) should be(List("Test 2"))
      }
    }
    "tear down" in {
      Time.reset()
    }
  }

  @tailrec
  private def waitForExists(path: Path, timeout: Long = 5.seconds.toMillis): Boolean = if (Files.exists(path)) {
    true
  } else if (timeout >= 0L) {
    Thread.sleep(1.second.toMillis)
    waitForExists(path, timeout - 1.second.toMillis)
  } else {
    false
  }

  @tailrec
  private def waitForDeleted(path: Path, timeout: Long = 5.seconds.toMillis): Boolean = if (!Files.exists(path)) {
    false
  } else if (timeout >= 0L) {
    Thread.sleep(1.second.toMillis)
    waitForDeleted(path, timeout - 1.second.toMillis)
  } else {
    true
  }

  @tailrec
  private def linesFor(path: Path, linesMinimum: Int = 1, waitForData: Long = 5.seconds.toMillis): List[String] = {
    if (Files.exists(path)) {
      val lines = Files.lines(path).toArray.toList.asInstanceOf[List[String]].map(_.trim).filter(_.nonEmpty)
      if (lines.nonEmpty && lines.size >= linesMinimum) {
        lines
      } else if (waitForData > 0L) {
        Thread.sleep(1.second.toMillis)
        linesFor(path, linesMinimum, waitForData - 1.second.toMillis)
      } else {
        lines
      }
    } else {
      Nil
    }
  }
}