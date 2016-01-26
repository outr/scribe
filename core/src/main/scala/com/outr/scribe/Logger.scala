package com.outr.scribe

case class Logger(name: String,
                  parent: Option[Logger] = Some(Logger.Root),
                  multiplier: Double = 1.0,
                  includeTrace: Boolean = false) {
  private[scribe] var handlers = Set.empty[LogHandler]

  def trace(message: => Any): Unit = log(Level.Trace, message)
  def debug(message: => Any): Unit = log(Level.Debug, message)
  def info(message: => Any): Unit = log(Level.Info, message)
  def warn(message: => Any): Unit = log(Level.Warn, message)
  def error(message: => Any): Unit = log(Level.Error, message)

  def log(level: Level, message: => Any): Unit = if (accepts(level.value)) {
    val record = if (includeTrace) {
      val (methodName, lineNumber) = LogRecord.trace(name, level)
      LogRecord(name, level, level.value * multiplier, () => message, methodName, lineNumber)
    } else {
      LogRecord(name, level, level.value * multiplier, () => message)
    }
    log(record)
  }

  protected[scribe] def log(record: LogRecord): Unit = {
    handlers.foreach(h => h.log(record))
    parent.foreach(p => p.log(record.copy(value = record.value * p.multiplier)))
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