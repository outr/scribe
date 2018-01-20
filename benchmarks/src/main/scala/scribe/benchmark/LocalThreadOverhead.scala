package scribe.benchmark

import java.util.concurrent.TimeUnit

import org.apache.logging.log4j.LogManager
import org.openjdk.jmh.annotations
import scribe.writer.FileWriter
import scribe.{LogHandler, Logger}
import scribe2.{Level, LogRecord}

// jmh:run -i 3 -wi 3 -f1 -t1
@annotations.State(annotations.Scope.Thread)
class LocalThreadOverhead {
  private lazy val log4jLogger = LogManager.getRootLogger
  private lazy val scribeLogger = {
    val l = Logger.byName("test").copy(parentName = None)
    l.addHandler(LogHandler(writer = FileWriter.flat("scribe", append = false)))
    l
  }
  private lazy val scribe2Logger = scribe2.Logger.root

  assert(log4jLogger.isInfoEnabled, "INFO is not enabled in log4j!")

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
  def withScribe1(): Unit = {
    var i = 0
    while (i < 1000) {
      scribeLogger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribe2Async(): Unit = {
    var i = 0
    while (i < 1000) {
//      scribe2Logger.info("test")
      scribe2Logger.log(LogRecord(Level.Info, Level.Info.value, "test", "", None, None, Thread.currentThread(), System.currentTimeMillis()))
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribe2Sync(): Unit = {
    scribe2.Logger.asynchronous = false
    var i = 0
    while (i < 1000) {
      //      scribe2Logger.info("test")
      scribe2Logger.log(LogRecord(Level.Info, Level.Info.value, "test", "", None, None, Thread.currentThread(), System.currentTimeMillis()))
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4j(): Unit = {
    var i = 0
    while (i < 1000) {
      log4jLogger.info("test")
      i += 1
    }
  }
}