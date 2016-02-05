package com.outr.scribe

import scala.language.experimental.macros

/**
  * Logger is the class to which all logging calls are made. The primary use-case of Logger is via use of the Logging
  * trait that may be mixed-in to any class.
  *
  * @param name the name of this logger
  * @param parent the parent logger (defaults to Logger.Root)
  * @param multiplier the multiplier that should be applied to boost the value of all messages routed through this
  *                   logger (Defaults to 1.0)
  */
case class Logger(name: String,
                  parent: Option[Logger] = Some(Logger.Root),
                  multiplier: Double = 1.0) {
  private[scribe] var handlers = Set.empty[LogHandler]

  /**
    * Trace log entry. Uses Macros to optimize performance.
    */
  def trace(message: => Any): Unit = macro Macros.trace

  /**
    * Debug log entry. Uses Macros to optimize performance.
    */
  def debug(message: => Any): Unit = macro Macros.debug

  /**
    * Info log entry. Uses Macros to optimize performance.
    */
  def info(message: => Any): Unit = macro Macros.info

  /**
    * Warn log entry. Uses Macros to optimize performance.
    */
  def warn(message: => Any): Unit = macro Macros.warn

  /**
    * Error log entry. Uses Macros to optimize performance.
    */
  def error(message: => Any): Unit = macro Macros.error

  /**
    * Log method invoked by trace, debug, info, warn, and error. Ideally should not be called directly as it will not
    * be able to take advantage of Macro optimizations.
    *
    * @param level the logging level
    * @param message function to derive the message for the log
    * @param methodName the method name if applicable
    * @param lineNumber the line number the logging was invoked on
    */
  def log(level: Level,
          message: => Any,
          methodName: Option[String] = None,
          lineNumber: Int = -1): Unit = if (accepts(level.value)) {
    val record = LogRecord(name, level, level.value * multiplier, () => message, methodName, lineNumber)
    log(record)
  }

  protected[scribe] def log(record: LogRecord): Unit = {
    handlers.foreach(h => h.log(record))
    parent.foreach(p => p.log(record.updateValue(record.value * p.multiplier)))
  }

  /**
    * Returns true if the supplied value will be accepted by a handler of this logger or an ancestor (up the parent
    * tree)
    */
  def accepts(value: Double): Boolean = {
    val v = value * multiplier

    handlers.exists(handler => handler.accepts(v)) || parent.exists(p => p.accepts(v))
  }

  /**
    * Adds a handler that will receive log records submitted to this logger and any descendant loggers.
    */
  def addHandler(handler: LogHandler): Unit = synchronized {
    handlers += handler
  }

  /**
    * Removes an handler that was previously added to this logger.
    */
  def removeHandler(handler: LogHandler): Unit = synchronized {
    handlers -= handler
  }

  /**
    * Removes all handlers currently on this logger.
    */
  def clearHandlers(): Unit = synchronized {
    handlers = Set.empty
  }
}

object Logger {
  val systemOut = System.out
  val systemErr = System.err

  /**
    * The root logger is the default parent of all loggers and comes default with a default LogHandler added.
    */
  val Root: Logger = {
    val l = Logger("root", parent = None)
    l.addHandler(LogHandler())
    l
  }
}