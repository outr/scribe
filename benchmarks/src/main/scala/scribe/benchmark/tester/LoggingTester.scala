package scribe.benchmark.tester

trait LoggingTester {
  lazy val name: String = getClass.getSimpleName.replace("LoggingTester", "")

  def init(): Unit = {}

  def run(messages: Iterator[String]): Unit

  def dispose(): Unit = {}
}
