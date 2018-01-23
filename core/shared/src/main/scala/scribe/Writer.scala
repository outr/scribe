package scribe2

trait Writer {
  def write(output: String): Unit

  def dispose(): Unit = {}
}
