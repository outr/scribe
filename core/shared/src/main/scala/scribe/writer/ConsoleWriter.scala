package scribe.writer

import scribe.{LogRecord, Logger}

object ConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: String): Unit = Logger.system.out.print(output)
}