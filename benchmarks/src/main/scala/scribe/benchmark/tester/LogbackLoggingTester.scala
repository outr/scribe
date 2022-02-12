package scribe.benchmark.tester

class LogbackLoggingTester extends LoggingTester {
  override def run(messages: Iterator[String]): Unit = {
    val logger = org.slf4j.LoggerFactory.getLogger("logback")
    messages.foreach(logger.info)
  }
}