package scribe.benchmark

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import com.typesafe.{scalalogging => sc}
import org.apache.logging.log4j.LogManager
import org.openjdk.jmh.annotations
import org.pmw.tinylog
import scribe._
import scribe.format._
import scribe.handler.AsynchronousLogHandler

// jmh:run -i 3 -wi 3 -f1 -t1 -rf JSON -rff benchmarks.json
@annotations.State(annotations.Scope.Thread)
@annotations.Fork(jvmArgs = Array("-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"))
class LoggingSpeedBenchmark {
  assert(LogManager.getRootLogger.isInfoEnabled, "INFO is not enabled in log4j!")

  private var synchronousWriter: writer.FileWriter = _
  private var synchronousLogger: Logger = _

  private var asynchronousWriter: writer.FileWriter = _
  private var asynchronousHandler: AsynchronousLogHandler = _
  private var asynchronousLogger: Logger = _

  private var log4jLogger: org.apache.logging.log4j.Logger = _

  private var log4jTraceLogger: org.apache.logging.log4j.Logger = _

  private var log4sLogger: org.log4s.Logger = _

  private var logbackLogger: org.slf4j.Logger = _

  private var scalaLogger: sc.Logger = _

  @annotations.Setup
  def doSetup(): Unit = {
    ConfigFactory.load()

    tinylog.Configurator
    .defaultConfig()
    .removeAllWriters()
    .writer(new tinylog.writers.FileWriter("logs/tiny.log"))
    .level(tinylog.Level.INFO)
    .activate()

    asynchronousWriter =  writer.FileWriter.simple("scribe-async.log")
    asynchronousHandler = AsynchronousLogHandler(Formatter.default, asynchronousWriter)
    asynchronousLogger = Logger.empty.orphan().withHandler(asynchronousHandler)

    synchronousWriter = writer.FileWriter.simple("scribe.log")
    synchronousLogger = Logger.empty.orphan().withHandler(writer = synchronousWriter)

    log4jLogger = LogManager.getRootLogger
    log4jTraceLogger = LogManager.getLogger("Trace")

    log4sLogger = org.log4s.getLogger("scala")

    logbackLogger = org.slf4j.LoggerFactory.getLogger("logback")

    scalaLogger = sc.Logger("root")
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribe(): Unit = {
    var i = 0
    while (i < 1000) {
      synchronousLogger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeAsync(): Unit = {
    var i = 0
    while (i < 1000) {
      asynchronousLogger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4j(): Unit = {
    var i = 0
    while (i < 1000) {
      log4jLogger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4s(): Unit = {
    var i = 0
    while (i < 1000) {
      log4sLogger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4jTrace(): Unit = {
    var i = 0
    while (i < 1000) {
      log4jTraceLogger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScalaLogging(): Unit = {
    var i = 0
    while (i < 1000) {
      scalaLogger.info("test")
      i += 1
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLogback(): Unit = {
    var i = 0
    while (i < 1000) {
      logbackLogger.info("test")
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
    synchronousWriter.dispose()
    asynchronousWriter.dispose()
  }
}