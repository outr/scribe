package scribe.benchmark

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations
import scribe.benchmark.tester.{Log4CatsLoggingTester, Log4JLoggingTester, Log4JTraceLoggingTester, Log4SLoggingTester, LogbackLoggingTester, LoggingTester, ScalaLoggingLoggingTester, ScribeAsyncLoggingTester, ScribeEffectLoggingTester, ScribeEffectParallelLoggingTester, ScribeLoggingTester, TinyLogLoggingTester}

// jmh:run -i 3 -wi 3 -f1 -t1 -rf JSON -rff benchmarks.json
@annotations.State(annotations.Scope.Thread)
class LoggingSpeedBenchmark {
  val Iterations: Int = 1000

  private val log4cats = new Log4CatsLoggingTester
  private val log4j = new Log4JLoggingTester
  private val log4jTrace = new Log4JTraceLoggingTester
  private val log4s = new Log4SLoggingTester
  private val logback = new LogbackLoggingTester
  private val scalaLogging = new ScalaLoggingLoggingTester
  private val scribeAsync = new ScribeAsyncLoggingTester
  private val scribeEffect = new ScribeEffectLoggingTester
  private val scribeEffectParallel = new ScribeEffectParallelLoggingTester
  private val scribe = new ScribeLoggingTester
  private val tinyLog = new TinyLogLoggingTester

  private val testers = List(
    log4cats, log4j, log4jTrace, log4s, logback, scalaLogging, scribeAsync, scribeEffect, scribeEffectParallel, scribe,
    tinyLog
  )

  @annotations.Setup(annotations.Level.Trial)
  def doSetup(): Unit = testers.foreach(_.init())

  private def withTester(tester: LoggingTester): Unit = {
    val messages = (0 until Iterations).map(i => s"Test $i")
    tester.run(messages.iterator)
  }

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribe(): Unit = withTester(scribe)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeEffect(): Unit = withTester(scribeEffect)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeEffectParallel(): Unit = withTester(scribeEffectParallel)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScribeAsync(): Unit = withTester(scribeAsync)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4j(): Unit = withTester(log4j)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
  //  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4cats(): Unit = withTester(log4cats)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4s(): Unit = withTester(log4s)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLog4jTrace(): Unit = withTester(log4jTrace)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withScalaLogging(): Unit = withTester(scalaLogging)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withLogback(): Unit = withTester(logback)

  @annotations.Benchmark
  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime))
//  @annotations.BenchmarkMode(Array(annotations.Mode.AverageTime, annotations.Mode.SampleTime, annotations.Mode.Throughput))
  @annotations.OutputTimeUnit(TimeUnit.NANOSECONDS)
  @annotations.OperationsPerInvocation(1000)
  def withTinyLog(): Unit = withTester(tinyLog)

  @annotations.TearDown
  def tearDown(): Unit = testers.foreach(_.dispose())
}