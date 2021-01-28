package scribe.handler

import scribe.LogRecord

trait LogHandle {
  def log[M](handler: LogHandlerBuilder, record: LogRecord[M]): Unit
}