package scribe.jpl

import scribe.{Level, LogRecord}
import scribe.message.LoggableMessage
import scribe.throwable.TraceLoggableMessage

import java.lang.System.Logger
import java.text.MessageFormat
import java.util.ResourceBundle
import scala.util.control.NonFatal

class ScribeSystemLogger(name: String) extends System.Logger {

  private val delegate = scribe.Logger(name)

  def getName: String = name

  def isLoggable(level: Logger.Level): Boolean =
    delegate.includes(translate(level))

  def log(
      level: Logger.Level,
      bundle: ResourceBundle,
      msg: String,
      thrown: Throwable
  ): Unit = {
    val scribeLevel = translate(level)
    if (delegate.includes(scribeLevel)) {
      val message = localize(bundle, msg)
      log(
        scribeLevel,
        LoggableMessage.string2LoggableMessage(message) :: Option(thrown).map(t => TraceLoggableMessage(t)).toList
      )
    }
  }

  def log(
      level: Logger.Level,
      bundle: ResourceBundle,
      format: String,
      params: AnyRef*
  ): Unit = {
    val scribeLevel = translate(level)
    if (delegate.includes(scribeLevel)) {
      val msg = if (params != null && params.nonEmpty) {
        unsafeFormat(bundle, format, params: _*)
      } else {
        localize(bundle, format)
      }
      log(scribeLevel, List(LoggableMessage.string2LoggableMessage(msg)))
    }
  }

  private def log(level: Level, messages: List[LoggableMessage]): Unit =
    delegate.log(
      LogRecord(
        level = level,
        levelValue = level.value,
        messages = messages,
        fileName = "",
        className = name,
        methodName = None,
        line = None,
        column = None
      )
    )

  private def translate(level: Logger.Level): Level = level match {
    case Logger.Level.ALL => Level.Trace
    case Logger.Level.TRACE => Level.Trace
    case Logger.Level.DEBUG => Level.Debug
    case Logger.Level.INFO => Level.Info
    case Logger.Level.WARNING => Level.Warn
    case Logger.Level.ERROR => Level.Error
    case Logger.Level.OFF => Level.Fatal
  }

  private def localize(bundle: ResourceBundle, msg: String): String =
    if (bundle == null || msg == null) {
      msg
    } else {
      unsafeGet(bundle, msg)
    }

  private def unsafeFormat(bundle: ResourceBundle, msg: String, params: AnyRef*): String =
    if (msg == null) {
      msg
    } else if (bundle == null) {
      MessageFormat.format(msg, params: _*)
    } else {
      MessageFormat.format(unsafeGet(bundle, msg), params: _*)
    }

  private def unsafeGet(bundle: ResourceBundle, msg: String): String = try {
    bundle.getString(msg)
  } catch {
    case _: ClassCastException => bundle.getObject(msg).toString
    case NonFatal(_) => msg
  }

}
