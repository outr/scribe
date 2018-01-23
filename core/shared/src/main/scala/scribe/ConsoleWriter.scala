package scribe2

object ConsoleWriter extends Writer {
  override def write(output: String): Unit = Logger.system.out.print(output)
}