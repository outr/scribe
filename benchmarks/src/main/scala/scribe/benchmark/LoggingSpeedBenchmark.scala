package scribe.benchmark

import java.util.concurrent.TimeUnit

import org.apache.logging.log4j.LogManager
import org.openjdk.jmh.annotations
import org.openjdk.jmh.annotations.TearDown
import scribe._
import scribe.format._

// jmh:run -i 3 -wi 3 -f1 -t1
@annotations.State(annotations.Scope.Thread)
class LoggingSpeedBenchmark {
  assert(LogManager.getRootLogger.isInfoEnabled, "INFO is not enabled in log4j!")

  /**
    * Subtract the time of this benchmark from the other benchmarks.
    */
  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def baseLine(): Int = {
    var i = 0
    while (i < 1000) {
      i += 1
    }
    i
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribe(): Unit = {
    val fileWriter = writer.FileWriter.single("scribe")
    val logger = scribe.Logger.update(scribe.Logger.rootName) { l =>
      //formatter"$date [$threadName] $levelPaddedRight $positionAbbreviated - $message$newLine"
      l.clearHandlers()
        .withHandler(
          LogHandler
            .default
            .withFormatter(formatter"$date $threadName $levelPaddedRight $positionAbbreviated - $message$newLine")
            .withWriter(fileWriter)
        )
    }

    var i = 0
    while (i < 1000) {
//      scribe2Logger.info("test")
      logger.log(LogRecord(Level.Info, Level.Info.value, "test", "", None, None, Thread.currentThread(), System.currentTimeMillis()))
      i += 1
    }
    fileWriter.dispose()
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeAsync(): Unit = {
    val fileWriter = writer.FileWriter.single("scribe-async")
    val logger = scribe.Logger.update(scribe.Logger.rootName) { l =>
      //formatter"$date [$threadName] $levelPaddedRight $positionAbbreviated - $message$newLine"
      l.clearHandlers()
        .withHandler(
          AsynchronousLogHandler
            .default
            .withFormatter(formatter"[$date] $threadName $levelPaddedRight $positionAbbreviated - $message$newLine")
            .withWriter(fileWriter)
        )
    }

    var i = 0
    while (i < 1000) {
      //      scribe2Logger.info("test")
      logger.log(LogRecord(Level.Info, Level.Info.value, "test", "", None, None, Thread.currentThread(), System.currentTimeMillis()))
      i += 1
    }
    fileWriter.dispose()
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4j(): Unit = {
    val logger = LogManager.getRootLogger
    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
  }

  @TearDown
  def tearDown(): Unit = {
    AsynchronousLogHandler.dispose()
  }
}