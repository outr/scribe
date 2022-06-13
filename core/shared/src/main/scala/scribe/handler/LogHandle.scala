package scribe.handler

import scribe.LogRecord

trait LogHandle {
  def log(handler: LogHandlerBuilder, record: LogRecord): Unit
}