package scribe.file

object Platform {
  def addShutdownHook(f: => Unit): Unit = scala.scalanative.libc.stdlib.atexit(() => {
    try {
      f
    } catch {
      case exc: Throwable => {
        exc.printStackTrace()
        System.exit(1)
      }
    }
  })
}