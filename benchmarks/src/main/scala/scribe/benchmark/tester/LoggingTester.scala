package scribe.benchmark.tester

trait LoggingTester {
  def init(): Unit = {}

  def run(messages: Iterator[String]): Unit

  def dispose(): Unit = {}
}
