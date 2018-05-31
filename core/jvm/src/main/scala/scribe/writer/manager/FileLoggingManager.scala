package scribe.writer.manager

import scribe.LogRecord

trait FileLoggingManager {
  def derivePath: PathResolution
  def written[M](record: LogRecord[M], output: String): Unit = {}
}