package scribe.benchmark

import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit

import scribe.Logger
import scribe.format.Formatter
import scribe.writer.FileWriter
import scribe.writer.file.LogFileMode

object LoggingStressTest {
  def main(args: Array[String]): Unit = {
    val oneMillion = 1000000
    val oneHundredMillion = 100000000
    timed(oneHundredMillion, fileLogger(Formatter.default, LogFileMode.IO))
  }

  def stressAll(iterations: Int): Unit = {
    val types = List(
      "Null" -> nullLogger(),
      "NIO Simple" -> fileLogger(Formatter.simple, LogFileMode.NIO),
      "IO Simple" -> fileLogger(Formatter.simple, LogFileMode.IO),
      "NIO Default" -> fileLogger(Formatter.default, LogFileMode.NIO),
      "IO Default" -> fileLogger(Formatter.default, LogFileMode.IO)
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

  def nullLogger(): Logger = Logger.empty.orphan()

  def fileLogger(formatter: Formatter, mode: LogFileMode): Logger = {
    val path = Paths.get("logs/file-logging.log")
    Files.deleteIfExists(path)
    Logger.empty.orphan().withHandler(formatter, FileWriter().withMode(mode).path(_ => path))
  }

  def timed(iterations: Int, logger: Logger): Double = {
    val start = System.nanoTime()
    stressLogger(iterations, logger)
    val elapsed = System.nanoTime() - start
    TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) / 1000.0
  }

  def stressLogger(iterations: Int, logger: Logger): Unit = {
    logger.info("Testing logging")
    if (iterations > 0) {
      stressLogger(iterations - 1, logger)
    }
  }
}