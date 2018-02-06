package scribe.writer

object NullWriter extends Writer {
  override def write(output: String): Unit = {}
}
