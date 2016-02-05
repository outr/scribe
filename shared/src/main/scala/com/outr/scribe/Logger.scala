package com.outr.scribe

import scala.language.experimental.macros

case class Logger(name: String,
                  parent: Option[Logger] = Some(Logger.Root),
                  multiplier: Double = 1.0) {
  private[scribe] var handlers = Set.empty[LogHandler]

  def trace(message: => Any): Unit = macro Macros.trace
  def debug(message: => Any): Unit = macro Macros.debug
  def info(message: => Any): Unit = macro Macros.info
  def warn(message: => Any): Unit = macro Macros.warn
  def error(message: => Any): Unit = macro Macros.error

  def log(level: Level,
          message: => Any,
          methodName: Option[String] = None,
          lineNumber: Int = -1
         ): Unit =
    if (accepts(level.value)) {
      val record = LogRecord(name, level, level.value * multiplier, () => message, methodName, lineNumber)
      log(record)
    }

  protected[scribe] def log(record: LogRecord): Unit = {
    handlers.foreach(h => h.log(record))
    parent.foreach(p => p.log(record.updateValue(record.value * p.multiplier)))
  }

  def accepts(value: Double): Boolean = {
    val v = value * multiplier

    handlers.exists(handler => handler.accepts(v)) || parent.exists(p => p.accepts(v))
  }

  def addHandler(handler: LogHandler): Unit = synchronized {
    handlers += handler
  }

  def removeHandler(handler: LogHandler): Unit = synchronized {
    handlers -= handler
  }
}

object Logger {
  val systemOut = System.out
  val systemErr = System.err

  val Root: Logger = {
    val l = Logger("root", parent = None)
    l.addHandler(LogHandler())
    l
  }
}