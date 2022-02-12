package scribe.benchmark.tester

import scribe.Logger
import scribe.file._
import scribe.format.{message, _}

class ScribeLoggingTester extends LoggingTester {
  private lazy val fileWriter = FileWriter("logs" / "scribe.log")
  private lazy val formatter = formatter"$date $levelPaddedRight [$threadName] $message"
  private lazy val logger = Logger.empty.orphan().withHandler(formatter = formatter, writer = fileWriter)

  override def init(): Unit = logger

  override def run(messages: Iterator[String]): Unit = {
    messages.foreach { message =>
      logger.info(message)
    }
  }

  override def dispose(): Unit = fileWriter.dispose()
}