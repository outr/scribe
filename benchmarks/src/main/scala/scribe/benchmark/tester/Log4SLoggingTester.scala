package scribe.benchmark.tester

class Log4SLoggingTester extends LoggingTester {
  override def run(messages: Iterator[String]): Unit = {
    val logger = org.log4s.getLogger("scala")
    messages.foreach(msg => logger.info(msg))
  }
}