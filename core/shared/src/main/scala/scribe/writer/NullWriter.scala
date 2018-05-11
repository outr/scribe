package scribe.writer
import scribe.LogRecord

object NullWriter extends Writer {
  override def write[M](record: LogRecord[M], output: String): Unit = {}
}
