package scribe2

trait Writer {
  def write(record: LogRecord, formatter: Formatter): Unit
}
