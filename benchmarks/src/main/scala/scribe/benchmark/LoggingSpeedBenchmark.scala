package scribe.benchmark

import org.openjdk.jmh.annotations
import scribe.benchmark.tester._

import java.util.concurrent.TimeUnit

// jmh:run -i 3 -wi 3 -f1 -t1 -rf JSON -rff benchmarks.json
@annotations.State(annotations.Scope.Thread)
class LoggingSpeedBenchmark {
  val Iterations: Int = 1000

  private val t = new Testers

  @annotations.Setup(annotations.Level.Trial)
  def doSetup(): Unit = t.all.foreach(_.init())

  private def withTester(tester: LoggingTester): Unit = {
    val messages = (0 until Iterations).map(i => s"Test $i")
    tester.run(messages.iterator)
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribe(): Unit = withTester(t.scribe)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeEffect(): Unit = withTester(t.scribeEffect)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeEffectParallel(): Unit = withTester(t.scribeEffectParallel)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeAsync(): Unit = withTester(t.scribeAsync)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4j(): Unit = withTester(t.log4j)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4cats(): Unit = withTester(t.log4cats)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4s(): Unit = withTester(t.log4s)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4jTrace(): Unit = withTester(t.log4jTrace)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScalaLogging(): Unit = withTester(t.scalaLogging)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLogback(): Unit = withTester(t.logback)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withTinyLog(): Unit = withTester(t.tinyLog)

  @annotations.TearDown
  def tearDown(): Unit = t.all.foreach(_.dispose())
}