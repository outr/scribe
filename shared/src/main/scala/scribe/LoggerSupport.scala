package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  private[scribe] var handlers = Set.empty[LogHandler]

  def name: Option[String]
  def parentName: Option[String]
  def multiplier: Double

  def parent: Option[Logger] = parentName.map(Logger.byName)

  /**
    * Trace log entry. Uses Macros to optimize performance.
    */
  def trace(message: => Any): Unit = macro Macros.trace

  def trace(t: => Throwable): Unit = macro Macros.traceThrowable

  /**
    * Debug log entry. Uses Macros to optimize performance.
    */
  def debug(message: => Any): Unit = macro Macros.debug

  def debug(t: => Throwable): Unit = macro Macros.debugThrowable

  /**
    * Info log entry. Uses Macros to optimize performance.
    */
  def info(message: => Any): Unit = macro Macros.info

  def info(t: => Throwable): Unit = macro Macros.infoThrowable

  /**
    * Warn log entry. Uses Macros to optimize performance.
    */
  def warn(message: => Any): Unit = macro Macros.warn

  def warn(t: => Throwable): Unit = macro Macros.warnThrowable

  /**
    * Error log entry. Uses Macros to optimize performance.
    */
  def error(message: => Any): Unit = macro Macros.error

  /**
    * Error log entry. Uses Macros to optimize performance.
    */
  def error(t: => Throwable): Unit = macro Macros.errorThrowable

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
          className: String,
          methodName: Option[String] = None,
          lineNumber: Int = -1): Unit = if (accepts(level.value)) {
    val record = LogRecord(level, level.value * multiplier, () => message, className, methodName, lineNumber)
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
