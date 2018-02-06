package scribe.writer

trait Writer {
  def write(output: String): Unit

  def dispose(): Unit = {}
}