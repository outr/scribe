package scribe.benchmark.tester

import scribe.Logger
import scribe.file._
import scribe.format.{message, _}
import scribe.handler.AsynchronousLogHandle

class ScribeAsyncLoggingTester extends LoggingTester {
  private lazy val fileWriter = FileWriter("logs" / "scribe-async.log")
  private lazy val formatter = formatter"$date $levelPaddedRight [$threadName] $message"
  private lazy val asyncHandle = AsynchronousLogHandle()
  private lazy val logger = Logger.empty.orphan().withHandler(formatter = formatter, writer = fileWriter, handle = asyncHandle)

  override def init(): Unit = logger

  override def run(messages: Iterator[String]): Unit = {
    messages.foreach { message =>
      logger.info(message)
    }
  }

  override def dispose(): Unit = fileWriter.dispose()
}