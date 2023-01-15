package scribe.file

import scala.util.Try

object Platform {
  private var hooks = List.empty[() => Unit]

  scala.scalanative.libc.stdlib.atexit(Platform.callHooks _)

  def addShutdownHook(f: => Unit): Unit = synchronized {
    hooks = hooks ::: List(() => f)
  }

  def callHooks(): Unit = {
    hooks.foreach { f =>
      Try(f()).failed.foreach { throwable =>
        throwable.printStackTrace()
      }
    }
  }
}