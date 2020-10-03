package scribe.slf4j

import org.slf4j.Logger
import org.slf4j.helpers.{FormattingTuple, MarkerIgnoringBase, MessageFormatter}
import org.slf4j.spi.LocationAwareLogger
import scribe.Loggable.StringLoggable
import scribe.{LazyMessage, Level, LogRecord, Logger => ScribeLogger}

class ScribeLoggerAdapter(name: String) extends MarkerIgnoringBase with Logger {
  def scribeLevel(level: Int): Level = level match {
    case LocationAwareLogger.TRACE_INT => Level.Trace
    case LocationAwareLogger.DEBUG_INT => Level.Debug
    case LocationAwareLogger.INFO_INT => Level.Info
    case LocationAwareLogger.WARN_INT => Level.Warn
    case LocationAwareLogger.ERROR_INT => Level.Error
  }

  def log(level: Level, msg: String, t: Option[Throwable]): Unit = {
    val record = LogRecord(
      level = level,
      value = level.value,
      message = new LazyMessage(() => msg),
      loggable = StringLoggable,
      throwable = t,
      fileName = "",
      className = name,
      methodName = None,
      line = None,
      column = None
    )
    ScribeLogger(name).log(record)
  }

  def includes(level: Level): Boolean = ScribeLogger(name).includes(level)

  override def isTraceEnabled: Boolean = includes(Level.Trace)

  override def isDebugEnabled: Boolean = includes(Level.Debug)

  override def isInfoEnabled: Boolean = includes(Level.Info)

  override def isWarnEnabled: Boolean = includes(Level.Warn)

  override def isErrorEnabled: Boolean = includes(Level.Error)

  override def trace(msg: String): Unit = log(Level.Trace, msg, None)

  override def trace(format: String, arg: scala.Any): Unit = {
    logTuple(Level.Trace, MessageFormatter.format(format, arg))
  }

  override def trace(format: String, arg1: scala.Any, arg2: scala.Any): Unit = {
    logTuple(Level.Trace, MessageFormatter.format(format, arg1, arg2))
  }

  override def trace(format: String, arguments: AnyRef*): Unit = {
    logTuple(Level.Trace, MessageFormatter.arrayFormat(format, arguments.toArray))
  }

  override def trace(msg: String, t: Throwable): Unit = log(Level.Trace, msg, Option(t))

  override def debug(msg: String): Unit = log(Level.Debug, msg, None)

  override def debug(format: String, arg: scala.Any): Unit = {
    logTuple(Level.Debug, MessageFormatter.format(format, arg))
  }

  override def debug(format: String, arg1: scala.Any, arg2: scala.Any): Unit = {
    logTuple(Level.Debug, MessageFormatter.format(format, arg1, arg2))
  }

  override def debug(format: String, arguments: AnyRef*): Unit = {
    logTuple(Level.Debug, MessageFormatter.arrayFormat(format, arguments.toArray))
  }

  override def debug(msg: String, t: Throwable): Unit = log(Level.Debug, msg, Option(t))

  override def info(msg: String): Unit = log(Level.Info, msg, None)

  override def info(format: String, arg: scala.Any): Unit = {
    logTuple(Level.Info, MessageFormatter.format(format, arg))
  }

  override def info(format: String, arg1: scala.Any, arg2: scala.Any): Unit = {
    logTuple(Level.Info, MessageFormatter.format(format, arg1, arg2))
  }

  override def info(format: String, arguments: AnyRef*): Unit = {
    logTuple(Level.Info, MessageFormatter.arrayFormat(format, arguments.toArray))
  }

  override def info(msg: String, t: Throwable): Unit = log(Level.Info, msg, Option(t))

  override def warn(msg: String): Unit = log(Level.Warn, msg, None)

  override def warn(format: String, arg: scala.Any): Unit = {
    logTuple(Level.Warn, MessageFormatter.format(format, arg))
  }

  override def warn(format: String, arg1: scala.Any, arg2: scala.Any): Unit = {
    logTuple(Level.Warn, MessageFormatter.format(format, arg1, arg2))
  }

  override def warn(format: String, arguments: AnyRef*): Unit = {
    logTuple(Level.Warn, MessageFormatter.arrayFormat(format, arguments.toArray))
  }

  override def warn(msg: String, t: Throwable): Unit = log(Level.Warn, msg, Option(t))

  override def error(msg: String): Unit = log(Level.Error, msg, None)

  override def error(format: String, arg: scala.Any): Unit = {
    logTuple(Level.Error, MessageFormatter.format(format, arg))
  }

  override def error(format: String, arg1: scala.Any, arg2: scala.Any): Unit = {
    logTuple(Level.Error, MessageFormatter.format(format, arg1, arg2))
  }

  override def error(format: String, arguments: AnyRef*): Unit = {
    logTuple(Level.Error, MessageFormatter.arrayFormat(format, arguments.toArray))
  }

  override def error(msg: String, t: Throwable): Unit = log(Level.Error, msg, Option(t))

  private def logTuple(level: Level, tuple: FormattingTuple): Unit = {
    log(level, tuple.getMessage, Option(tuple.getThrowable))
  }
}