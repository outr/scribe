package scribe.benchmark

import scribe.benchmark.tester.{LoggingTester, Testers}

import java.util.concurrent.TimeUnit
import perfolation._

object LoggingVisualizations {
  val Iterations: Int = 10_000_000

  def main(args: Array[String]): Unit = {
    val testers = new Testers
    testers.all.foreach { tester =>
      benchmark(tester)
    }
  }

  def benchmark(tester: LoggingTester): Unit = {
    val initTime = elapsed(tester.init())
    val messages = (0 until Iterations).map(i => s"visualize $i").iterator
    val runTime = elapsed(tester.run(messages))
    val disposeTime = elapsed(tester.dispose())
    println(s"${tester.name}: Init: $initTime, Run: $runTime, Dispose: $disposeTime")
  }

  private def elapsed(f: => Unit): String = {
    val start = System.nanoTime()
    f
    val elapsed = System.nanoTime() - start
    val ms = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS)
    s"${(ms / 1000.0).f(f = 3)} seconds"
  }
}