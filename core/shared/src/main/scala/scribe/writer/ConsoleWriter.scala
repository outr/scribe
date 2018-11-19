package scribe.writer

import scribe._
import scribe.output.LogOutput

object ConsoleWriter extends Writer {
  // TODO: Support styled text
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = if (record.level <= Level.Info) {
    Logger.system.out.print(output.plainText)
  } else {
    Logger.system.err.print(output.plainText)
  }
}