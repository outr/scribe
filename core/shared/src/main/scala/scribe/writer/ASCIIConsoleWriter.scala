package scribe.writer

import scribe.{Level, LogRecord, Logger}
import scribe.output.LogOutput

import scala.math.Ordering.Implicits._
import scala.language.implicitConversions

object ASCIIConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    val stream = if (record.level <= Level.Info) {
      Logger.system.out
    } else {
      Logger.system.err
    }
    if (ConsoleWriter.SynchronizeWriting) {
      synchronized(stream.println(output.plainText))
    } else {
      stream.println(output.plainText)
    }
  }
}