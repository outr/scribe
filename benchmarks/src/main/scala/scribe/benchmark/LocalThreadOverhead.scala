package scribe.benchmark

import java.util.concurrent.TimeUnit

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.openjdk.jmh.annotations._
import scribe.Logger

// jmh:run -i 3 -wi 3 -f1 -t1
@State(Scope.Thread)
class LocalThreadOverhead {
  private lazy val log4jLogger = LogManager.getLogger("test")
  private lazy val scribeLogger = Logger.byName("test").copy(parentName = None)

  System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
  Configurator.initialize(ConfigurationBuilderFactory.newConfigurationBuilder().build())

  /**
    * Subtract the time of this benchmark from the other benchmarks.
    */
  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @OperationsPerInvocation(1000)
  def baseLine(): Int = {
    var i = 0
    while (i < 1000) {
      i += 1
    }
    i
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @OperationsPerInvocation(1000)
  def withScribe(): Unit = {
    var i = 0
    while (i < 1000) {
      scribeLogger.info("test")
      i += 1
    }
  }

//  @Benchmark
//  @BenchmarkMode(Array(Mode.AverageTime))
//  @OutputTimeUnit(TimeUnit.NANOSECONDS)
//  @OperationsPerInvocation(1000)
//  def withAsyncScribe(): Unit = {
//    var i = 0
//    while (i < 1000) {
//      asyncScribeLogger.info("test")
//      i += 1
//    }
//  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @OperationsPerInvocation(1000)
  def withLog4j(): Unit = {
    var i = 0
    while (i < 1000) {
      log4jLogger.info("test")
      i += 1
    }
  }

}