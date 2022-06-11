package scribe.benchmark

import org.openjdk.jmh.annotations
import scribe.format._
import scribe.{Level, LogRecord, Logger}
import scribe.handler.LogHandler
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@annotations.State(annotations.Scope.Thread)
class PerformanceBenchmark {
  private lazy val debug = new AtomicLong(0L)
  private lazy val info = new AtomicLong(0L)
  private lazy val error = new AtomicLong(0L)

  private lazy val logger = Logger.empty.orphan().withHandler(
    minimumLevel = Some(Level.Info),
    writer = new Writer {
      override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = record.level match {
        case Level.Debug => debug.incrementAndGet()
        case Level.Info => info.incrementAndGet()
        case Level.Error => error.incrementAndGet()
        case _ => // Ignore
      }
    }
  )

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withTrace(): Unit = {
    (0 until 1000).foreach { index =>
      logger.trace(s"Value: $index")
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withDebug(): Unit = {
    (0 until 1000).foreach { index =>
      logger.debug(s"Value: $index")
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withInfo(): Unit = {
    (0 until 1000).foreach { index =>
      logger.info(s"Value: $index")
    }
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withError(): Unit = {
    (0 until 1000).foreach { index =>
      logger.error(s"Value: $index")
    }
  }
}