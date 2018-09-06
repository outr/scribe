package scribe.writer

import scribe._

object ConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: String): Unit = if (record.level <= Level.Info) {
    Logger.system.out.print(output)
  } else {
    Logger.system.err.print(output)
  }
}