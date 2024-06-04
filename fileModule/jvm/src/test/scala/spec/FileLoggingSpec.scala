package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.file._
import scribe.format._
import scribe.output.format.{ASCIIOutputFormat, OutputFormat}
import scribe.util.Time
import scribe.{Level, Logger}

import java.io.File
import java.nio.file.{Files, Path}
import java.util.{Calendar, TimeZone}
import java.util.function.Consumer
import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.io.Source
import scala.language.implicitConversions

class FileLoggingSpec extends AnyWordSpec with Matchers {
  private var logger: Logger = Logger.empty.orphan()
  lazy val logFile: File = new File("logs/test.log")

  private var timeStamp: Long = 0L

  private val DateRegex = """(\d{4})-(\d{2})-(\d{2})""".r

  private def setDate(date: String): Unit = date match {
    case DateRegex(year, month, day) => {
      val c = Calendar.getInstance()
      c.set(year.toInt, month.toInt - 1, day.toInt, 0, 0, 0)
      timeStamp = c.getTimeInMillis
    }
  }

  private def incrementTime(duration: FiniteDuration): Unit = {
    timeStamp += duration.toMillis
  }

  private var writer: FileWriter = _

  private def setWriter(writer: FileWriter): Unit = {
    logger = logger.clearHandlers().withHandler(formatter = Formatter.simple, writer = writer)
    this.writer = writer
  }

  private def validateLogs(fileNames: String*): Unit = {
    writer.list().map(_.getName).toSet should be(fileNames.toSet)
  }

  "File Logging" when {
    "setup" in {
      TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"))
      OutputFormat.default = ASCIIOutputFormat
      setDate("2018-01-01")
      Time.function = () => timeStamp
    }
    "verifying simple logging" should {
      "configure logging to a temporary file" in {
        val directory = new File("logs")
        if (directory.exists()) {
          Files.newDirectoryStream(directory.toPath).forEach(new Consumer[Path] {
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
        setWriter(FileWriter(new File("logs") / ("app-" % year % "-" % month % "-" % day % ".log")).flushAlways)
      }
      "log to date formatted file" in {
        logger.info("Testing date formatted file")
      }
      "verify the date formatted file was logged to" in {
        val file = new File("logs/app-2018-01-01.log")
        waitForExists(file) should be(true)
        linesFor(file) should be(List("Testing date formatted file"))
        linesFor(logFile) should be(List("Testing File Logger"))
      }
      "change the timeStamp and write another log record" in {
        timeStamp += 1000 * 60 * 60 * 12
        logger.info("Testing mid-day")
      }
      "verify that two records are in the date formatted file" in {
        val file = new File("logs/app-2018-01-01.log")
        waitForExists(file) should be(true)
        linesFor(file, linesMinimum = 2) should be(List("Testing date formatted file", "Testing mid-day"))
        linesFor(logFile) should be(List("Testing File Logger"))
      }
      "increment timeStamp to the next day" in {
        timeStamp += 1000 * 60 * 60 * 12
        logger.info("Testing next day")
      }
      "verify that a new log file is created" in {
        val day1 = new File("logs/app-2018-01-01.log")
        val day2 = new File("logs/app-2018-01-02.log")
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
        val file = new File("logs/rolling.log")
        waitForExists(file) should be(true)
        linesFor(file) should be(List("Rolling 1"))
      }
      "increment date and roll file" in {
        writer.file.setLastModified(Time())
        setDate("2018-01-02")
        logger.info("Rolling 2")
      }
      "verify rolling log 2" in {
        val file = new File("logs/rolling.log")
        val rolled = new File("logs/rolling-2018-01-01.log")
        waitForExists(file) should be(true)
        waitForExists(rolled) should be(true)
        linesFor(file) should be(List("Rolling 2"))
        linesFor(rolled) should be(List("Rolling 1"))
      }
      "increment date and roll file again" in {
        writer.file.setLastModified(Time())
        setDate("2018-01-03")
        logger.info("Rolling 3")
      }
      "verify rolling log 3" in {
        val file = new File("logs/rolling.log")
        val rolled1 = new File("logs/rolling-2018-01-01.log")
        val rolled2 = new File("logs/rolling-2018-01-02.log")
        waitForExists(file) should be(true)
        waitForExists(rolled1) should be(true)
        waitForExists(rolled2) should be(true)
        linesFor(file) should be(List("Rolling 3"))
        linesFor(rolled1) should be(List("Rolling 1"))
        linesFor(rolled2) should be(List("Rolling 2"))
      }
      "verify the writer lists the logged files" in {
        validateLogs("rolling.log", "rolling-2018-01-01.log", "rolling-2018-01-02.log")
      }
    }
    // TODO: Disabled because of Scala 2.11 bug
    /*"set up specific scenario with two files two days old that" should {
      val f1 = new File("logs/r1.log")
      val f2 = new File("logs/r2.log")
      def l1: Logger = Logger("rolling.1")
      def l2: Logger = Logger("rolling.2")
      val twoDaysAgoMillis: Long = System.currentTimeMillis() - (48 * 60 * 60 * 1000)
      val twoDaysAgo: String = {
        val c = Calendar.getInstance()
        c.setTimeInMillis(twoDaysAgoMillis)
        s"${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH) + 1).f(i = 2)}-${c.get(Calendar.DAY_OF_MONTH).f(i = 2)}"
      }

      def filesAndContents(prefix: String): Set[(String, List[String])] = {
        val directory = new File("logs")
        directory.listFiles().filter(_.getName.startsWith(prefix)).map { f =>
          val source = Source.fromFile(f)
          val contents = try {
            source.getLines().toList
          } finally {
            source.close()
          }
          f.getName -> contents
        }.toSet
      }

      "delete all existing files by prefix" in {
        val directory = new File("logs")
        directory.listFiles().toList.filter(f => f.getName.startsWith("r1") || f.getName.startsWith("r2")).foreach { f =>
          f.delete()
        }
      }
      "verify no matching files exist" in {
        filesAndContents("r1").size should be(0)
        filesAndContents("r2").size should be(0)
      }
      "create and append to files" in {
        Time.reset()

        Files.write(f1.toPath, "old data".getBytes)
        Files.write(f2.toPath, "old data".getBytes)
        f1.setLastModified(twoDaysAgoMillis)
        f2.setLastModified(twoDaysAgoMillis)
      }
      "configure rolling logging" in {      // TODO: Figure out why this compile-errors on 2.11
        l1.orphan().withHandler(
          formatter = Formatter.simple,
          minimumLevel = Some(Level.Info),
          writer = FileWriter("logs" / ("r1" % rolling("-" % year % "-" % month % "-" % day) % ".log")).flushAlways
        ).replace()
        l2.orphan().withHandler(
          formatter = Formatter.simple,
          minimumLevel = Some(Level.Info),
          writer = FileWriter("logs" / ("r2" % rolling("-" % year % "-" % month % "-" % day) % ".log")).flushAlways
        ).replace()
      }
      "verify exactly two files" in {
        filesAndContents("r1") should be(Set("r1.log" -> List("old data")))
        filesAndContents("r2") should be(Set("r2.log" -> List("old data")))
      }
      "log a record to l1" in {
        l1.info("Testing 1")
      }
      "verify three files" in {
        filesAndContents("r1") should be(Set("r1.log" -> List("Testing 1"), s"r1-${twoDaysAgo}.log" -> List("old data")))
        filesAndContents("r2") should be(Set("r2.log" -> List("old data")))
      }
      "log a record to l2" in {
        l2.info("Testing 2")
      }
      "verify three files again" in {
        filesAndContents("r1") should be(Set("r1.log" -> List("Testing 1"), s"r1-${twoDaysAgo}.log" -> List("old data")))
        filesAndContents("r2") should be(Set("r2.log" -> List("Testing 2"), s"r2-${twoDaysAgo}.log" -> List("old data")))
      }
      "switch back the time function" in {
        Time.function = () => timeStamp
      }
    }*/
    "verifying GZIP support" should {
      "configure daily path with gzipping" in {
        setDate("2018-01-01")
        setWriter(FileWriter("logs" / ("gzip-" % year % "-" % month % "-" % day % ".log" % rollingGZIP())).flushAlways)
      }
      "log a record pre gzip" in {
        logger.info("Gzip 1")
      }
      "verify gzipping log 1" in {
        val file = new File("logs/gzip-2018-01-01.log")
        waitForExists(file) should be(true)
        linesFor(file) should be(List("Gzip 1"))
      }
      "modify date to create gzip" in {
        writer.file.setLastModified(Time())
        setDate("2018-01-02")
        logger.info("Gzip 2")
      }
      "verify gzipping log 2" in {
        val file = new File("logs/gzip-2018-01-02.log")
        val gzipped = new File("logs/gzip-2018-01-01.log.gz")
        val unGzipped = new File("logs/gzip-2018-01-01.log")
        waitForExists(file) should be(true)
        waitForExists(gzipped) should be(true)
        waitForExists(unGzipped) should be(false)
        linesFor(file) should be(List("Gzip 2"))
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
        val f1 = new File("logs/max.sized.log")
        val f2 = new File("logs/max.sized.1.log")
        val f3 = new File("logs/max.sized.2.log")
        waitForExists(f1) should be(true)
        waitForExists(f2) should be(true)
        waitForExists(f3) should be(true)
        linesFor(f1) should be(List("Record 3"))
        linesFor(f2) should be(List("Record 2"))
        linesFor(f3) should be(List("Record 1"))
      }
    }
    "verifying max number of log files" should {
      "configure maximum number of log files" in {
        setWriter(
          FileWriter("logs" / ("maxlogs" % maxSize(max = 1L, separator = ".") % maxLogs(3, 0.seconds) % ".log")).flushAlways
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
        val f1 = new File("logs/maxlogs.log")
        val f2 = new File("logs/maxlogs.1.log")
        val f3 = new File("logs/maxlogs.2.log")
        val f4 = new File("logs/maxlogs.3.log")
        waitForExists(f1) should be(true)
        waitForExists(f2) should be(true)
        waitForExists(f3) should be(true)
        waitForDeleted(f4) should be(false)
        linesFor(f1) should be(List("Record 4"))
        linesFor(f2) should be(List("Record 3"))
        linesFor(f3) should be(List("Record 2"))
      }
    }
    "verifying corner cases" should {
      "rolling logging for an existing log file should roll properly" in {
        val file1 = new File("logs/rolling1.log")
        val file2 = new File("logs/rolling1.2018.01.01.log")
        val file3 = new File("logs/rolling1.2018.01.02.log")
        file1.delete()
        file2.delete()
        file3.delete()

        setDate("2018-01-01")

        setWriter(
          FileWriter("logs" / ("rolling1" % rolling("." % daily(".")) % ".log")).flushAlways
        )
        writer.file should be(file1)
        val logger = Logger
          .empty
          .orphan()
          .withHandler(
            formatter = Formatter.simple,
            writer = writer,
            minimumLevel = Some(Level.Trace)
          )

        logger.debug("Test 1")
        linesFor(file1) should be(List("Test 1"))
        linesFor(file2) should be(Nil)
        linesFor(file3) should be(Nil)
        file1.exists() should be(true)
        file2.exists() should be(false)
        file3.exists() should be(false)

        incrementTime(5.seconds)
        writer.file.setLastModified(Time())
        logger.info("Test 2")
        linesFor(file1) should be(List("Test 1", "Test 2"))
        linesFor(file2) should be(Nil)
        linesFor(file3) should be(Nil)
        file1.exists() should be(true)
        file2.exists() should be(false)
        file3.exists() should be(false)

        writer.file.setLastModified(Time())
        setDate("2018-01-02")
        logger.debug("Test 3")
        linesFor(file1) should be(List("Test 3"))
        linesFor(file2) should be(List("Test 1", "Test 2"))
        linesFor(file3) should be(Nil)
        file1.exists() should be(true)
        file2.exists() should be(true)
        file3.exists() should be(false)

        writer.file.setLastModified(Time())
        setDate("2018-01-03")
        logger.debug("Test 4")
        linesFor(file1) should be(List("Test 4"))
        linesFor(file2) should be(List("Test 1", "Test 2"))
        linesFor(file3) should be(List("Test 3"))
      }
      "rolling logging for an old log file should roll properly" in {
        val file1 = new File("logs", "rolling2.log")
        val file2 = new File("logs", "rolling2.2018.01.01.log")
        val file3 = new File("logs", "rolling2.2018.01.02.log")
        file1.delete()
        file2.delete()
        file3.delete()

        // Write something to the rolling file
        Files.write(file1.toPath, "existing\n".getBytes)

        setDate("2018-01-01")
        file1.setLastModified(Time())

        setDate("2018-01-02")

        setWriter(
          FileWriter("logs" / ("rolling2" % rolling("." % daily(".")) % ".log")).flushAlways
        )
        writer.file should be(file1)
        val logger = Logger
          .empty
          .orphan()
          .withHandler(
            formatter = Formatter.simple,
            writer = writer,
            minimumLevel = Some(Level.Trace)
          )

        logger.info("testing")

        linesFor(file1) should be(List("testing"))
        linesFor(file2) should be(List("existing"))
        linesFor(file3) should be(Nil)
      }
    }
    "tear down" in {
      Time.reset()
    }
  }

  @tailrec
  private def waitForExists(file: File, timeout: Long = 5.seconds.toMillis): Boolean = if (file.exists()) {
    true
  } else if (timeout >= 0L) {
    Thread.sleep(1.second.toMillis)
    waitForExists(file, timeout - 1.second.toMillis)
  } else {
    false
  }

  @tailrec
  private def waitForDeleted(file: File, timeout: Long = 5.seconds.toMillis): Boolean = if (!file.exists()) {
    false
  } else if (timeout >= 0L) {
    Thread.sleep(1.second.toMillis)
    waitForDeleted(file, timeout - 1.second.toMillis)
  } else {
    true
  }

  @tailrec
  private def linesFor(file: File, linesMinimum: Int = 1, waitForData: Long = 5.seconds.toMillis): List[String] = {
    if (file.exists()) {
      val lines = {
        val source = Source.fromFile(file)
        try {
          source.getLines().toList.map(_.trim).filter(_.nonEmpty)
        } finally {
          source.close()
        }
      }
      if (lines.nonEmpty && lines.size >= linesMinimum) {
        lines
      } else if (waitForData > 0L) {
        Thread.sleep(1.second.toMillis)
        linesFor(file, linesMinimum, waitForData - 1.second.toMillis)
      } else {
        lines
      }
    } else {
      Nil
    }
  }
}