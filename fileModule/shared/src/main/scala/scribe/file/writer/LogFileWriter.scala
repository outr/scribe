package scribe.file.writer

import scribe.file.LogFile

trait LogFileWriter {
  def write(output: String): Unit

  def flush(): Unit

  def dispose(): Unit
}

object LogFileWriter {
  var default: LogFile => LogFileWriter = new IOLogFileWriter(_)

  def apply(logFile: LogFile): LogFileWriter = default(logFile)
}