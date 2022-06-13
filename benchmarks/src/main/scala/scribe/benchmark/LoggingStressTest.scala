package scribe.benchmark

import scribe.file._
import scribe.format.Formatter
import scribe.handler.LogHandler
import scribe.{Level, LogRecord, Logger}

import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit
import scala.annotation.tailrec

object LoggingStressTest {
  private var logged = 0

  def main(args: Array[String]): Unit = {
    val oneMillion = 1000000
    val fiveMillion = 5000000
    val oneHundredMillion = 100000000
    val elapsed = timed(fiveMillion, nullLogger())
//    val elapsed = timed(fiveMillion, fileLogger(Formatter.default, LogFileMode.IO))
    scribe.info(s"Ran in $elapsed seconds, Logged: $logged")
  }

  def stressAll(iterations: Int): Unit = {
    val types = List(
      "Null" -> nullLogger(),
      "Simple" -> fileLogger(Formatter.simple),
      "Default" -> fileLogger(Formatter.classic)
    )
    types.foreach {
      case (name, logger) => {
        val elapsed = timed(iterations, logger)
        scribe.info(s"$iterations for $name in $elapsed seconds")
      }
    }
    scribe.info("Reversing!")
    types.reverse.foreach {
      case (name, logger) => {
        val elapsed = timed(iterations, logger)
        scribe.info(s"$iterations for $name in $elapsed seconds")
      }
    }
    scribe.info("Completed!")
  }

  def nullLogger(): Logger = Logger("nullLogger").orphan().withHandler(new LogHandler {
    override def log(record: LogRecord): Unit = logged += 1
  }).replace()

  def fileLogger(formatter: Formatter): Logger = {
    val path = Paths.get("logs/file-logging.log")
    Files.deleteIfExists(path)
    Logger.empty.orphan().withHandler(formatter, FileWriter(path), minimumLevel = Some(Level.Info)).replace()
  }

  def timed(iterations: Int, logger: Logger): Double = {
    val start = System.nanoTime()
    hierarchicalStrain(iterations, logger)
    val elapsed = System.nanoTime() - start
    TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) / 1000.0
  }

  @tailrec
  def stressLogger(iterations: Int, logger: Logger): Unit = {
    logger.info("Testing logging")
    if (iterations > 0) {
      stressLogger(iterations - 1, logger)
    }
  }

  def hierarchicalStrain(iterations: Int, logger: Logger): Unit = {
    val sub1 = Logger().withParent(logger).replace()
    val sub2 = Logger().withParent(sub1).replace()
    val sub3 = Logger().withParent(sub2).replace()
    (0 until iterations).foreach { index =>
      sub3.info(s"INFO $index")
      sub3.debug(s"DEBUG $index")
      sub3.trace(s"TRACE $index")
    }
  }
}