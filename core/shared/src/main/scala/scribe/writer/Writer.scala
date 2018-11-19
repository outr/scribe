package scribe.writer

import scribe.LogRecord
import scribe.output.LogOutput

trait Writer {
  def write[M](record: LogRecord[M], output: LogOutput): Unit

  def dispose(): Unit = {}
}