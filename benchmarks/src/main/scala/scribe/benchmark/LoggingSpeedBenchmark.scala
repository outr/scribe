package scribe.benchmark

import java.util.concurrent.TimeUnit

import com.typesafe.{scalalogging => sc}
import org.apache.logging.log4j.LogManager
import org.openjdk.jmh.annotations
import org.openjdk.jmh.annotations.TearDown
import scribe._
import scribe.format._

// jmh:run -i 3 -wi 3 -f1 -t1 -rf JSON -rff benchmarks.json
@annotations.State(annotations.Scope.Thread)
class LoggingSpeedBenchmark {
  assert(LogManager.getRootLogger.isInfoEnabled, "INFO is not enabled in log4j!")

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
      l.clearHandlers().withHandler(LogHandler(Formatter.default, fileWriter))
    }

    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
    fileWriter.dispose()
  }

  // TODO: figure out why this shrivels up and dies
  /*@annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeAsync(): Unit = {
    val fileWriter = writer.FileWriter.single("scribe-async")
    val logger = scribe.Logger.update(scribe.Logger.rootName) { l =>
      l.clearHandlers().withHandler(AsynchronousLogHandler(Formatter.default, fileWriter))
    }

    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
    fileWriter.dispose()
  }*/

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

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4jTrace(): Unit = {
    val logger = LogManager.getLogger("Trace")
    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScalaLogging(): Unit = {
    val logger = sc.Logger("root")
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