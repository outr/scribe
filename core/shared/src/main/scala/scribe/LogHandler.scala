package scribe2

trait LogHandler {
  def log(record: LogRecord): Unit
}
