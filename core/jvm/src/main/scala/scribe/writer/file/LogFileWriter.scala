package scribe.writer.file

trait LogFileWriter {
  def write(output: String): Unit

  def flush(): Unit

  def dispose(): Unit
}
