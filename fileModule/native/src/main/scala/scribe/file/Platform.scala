package scribe.file

import scala.util.Try

object Platform {
  def addShutdownHook(f: => Unit): Unit = scala.scalanative.libc.stdlib.atexit(() => Try(f).failed.foreach { t =>
    t.printStackTrace()
    sys.exit(1)
  })
}