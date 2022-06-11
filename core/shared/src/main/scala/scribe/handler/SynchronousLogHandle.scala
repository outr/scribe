package scribe.handler

import scribe.LogRecord
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.output.format.OutputFormat
import scribe.writer.{ConsoleWriter, Writer}

object SynchronousLogHandle extends LogHandle {
  def log(handler: LogHandlerBuilder, record: LogRecord): Unit = {
    record.modify(handler.modifiers).foreach { r =>
      val logOutput = handler.formatter.format(r)
      handler.writer.write(record, logOutput, handler.outputFormat)
    }
  }
}