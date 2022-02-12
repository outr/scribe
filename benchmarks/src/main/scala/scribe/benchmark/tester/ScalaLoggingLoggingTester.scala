package scribe.benchmark.tester

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging._

class ScalaLoggingLoggingTester extends LoggingTester {
  override def init(): Unit = ConfigFactory.load()

  override def run(messages: Iterator[String]): Unit = {
    val logger = Logger("root")
    messages.foreach(msg => logger.info(msg))
  }
}