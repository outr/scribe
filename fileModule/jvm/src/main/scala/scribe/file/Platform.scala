package scribe.file

object Platform {
  def addShutdownHook(f: => Unit): Unit = Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = f
  })
}