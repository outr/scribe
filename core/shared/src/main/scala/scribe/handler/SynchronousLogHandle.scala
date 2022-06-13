package scribe.handler

import scribe.LogRecord

object SynchronousLogHandle extends LogHandle {
  def log(handler: LogHandlerBuilder, record: LogRecord): Unit = {
    record.modify(handler.modifiers).foreach { r =>
      val logOutput = handler.formatter.format(r)
      handler.writer.write(record, logOutput, handler.outputFormat)
    }
  }
}