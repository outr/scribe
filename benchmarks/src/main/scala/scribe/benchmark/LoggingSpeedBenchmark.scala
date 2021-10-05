package scribe.benchmark

import java.util.concurrent.TimeUnit
import com.typesafe.config.ConfigFactory
import com.typesafe.{scalalogging => sc}
import org.apache.logging.log4j.LogManager
import org.openjdk.jmh.annotations
import org.pmw.tinylog
import scribe._
import scribe.file._
import scribe.format._
import scribe.handler.AsynchronousLogHandle

// jmh:run -i 3 -wi 3 -f1 -t1 -rf JSON -rff benchmarks.json
@annotations.State(annotations.Scope.Thread)
class LoggingSpeedBenchmark {
  assert(LogManager.getRootLogger.isInfoEnabled, "INFO is not enabled in log4j!")

//  private lazy val asynchronousWriter = FileWriter("logs" / "scribe-async.log")
//  private lazy val asynchronousHandler = AsynchronousLogHandler(Formatter.classic, asynchronousWriter)

  @annotations.Setup(annotations.Level.Trial)
  def doSetup(): Unit = {
    ConfigFactory.load()
    tinylog.Configurator
      .defaultConfig()
      .removeAllWriters()
      .writer(new tinylog.writers.FileWriter("logs/tiny.log"))
      .level(tinylog.Level.INFO)
      .activate()
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribe(): Unit = {
    val fileWriter = FileWriter("logs" / "scribe.log")
    val formatter = formatter"$date $levelPaddedRight [$threadName] $message"
    val logger = Logger.empty.orphan().withHandler(formatter = formatter, writer = fileWriter)

    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
    fileWriter.dispose()
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeAsync(): Unit = {
    val logger = Logger.empty.orphan().withHandler(handle = AsynchronousLogHandle())

    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
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
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4s(): Unit = {
    val logger = org.log4s.getLogger("scala")
    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
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
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
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

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLogback(): Unit = {
    val logger = org.slf4j.LoggerFactory.getLogger("logback")
    var i = 0
    while (i < 1000) {
      logger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withTinyLog(): Unit = {
    var i = 0
    while (i < 1000) {
      tinylog.Logger.info("test")
      i += 1
    }
  }

  /*@annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withPrintLine(): Unit = {
    var i = 0
    while (i < 1000) {
      println("test")
      i += 1
    }
  }*/

  @annotations.TearDown
  def tearDown(): Unit = {
//    asynchronousWriter.dispose()
  }
}