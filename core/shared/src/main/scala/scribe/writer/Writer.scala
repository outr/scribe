package scribe.writer

import scribe.LogRecord

trait Writer {
  def write[M](record: LogRecord[M], output: String): Unit

  def dispose(): Unit = {}
}