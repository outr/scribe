package scribe.benchmark.tester

import org.apache.logging.log4j.LogManager

class Log4JTraceLoggingTester extends LoggingTester {
  override def run(messages: Iterator[String]): Unit = {
    val logger = LogManager.getLogger("Trace")
    messages.foreach(logger.info)
  }

  override def dispose(): Unit = LogManager.shutdown()
}