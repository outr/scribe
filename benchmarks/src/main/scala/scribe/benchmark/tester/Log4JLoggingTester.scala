package scribe.benchmark.tester

import org.apache.logging.log4j.LogManager

class Log4JLoggingTester extends LoggingTester {
  override def init(): Unit = assert(LogManager.getRootLogger.isInfoEnabled, "INFO is not enabled in log4j!")

  override def run(messages: Iterator[String]): Unit = {
    val logger = LogManager.getRootLogger
    messages.foreach(logger.info)
  }

  override def dispose(): Unit = LogManager.shutdown()
}