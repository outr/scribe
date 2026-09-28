package scribe.jul

import scribe._
import scribe.message.LoggableMessage
import scribe.throwable.TraceLoggableMessage

import java.util.logging.{Level => JLevel}

object JULHandler extends java.util.logging.Handler {
  override def publish(record: java.util.logging.LogRecord): Unit = {
    // A record can come without a logger name (built directly and passed to `Logger.log(LogRecord)`): it goes to root.
    val name = Option(record.getLoggerName).filter(_.nonEmpty)
    val logger = name.fold(scribe.Logger.root)(scribe.Logger(_))
    val level = l2l(record.getLevel)
    val message: LoggableMessage = format(record)
    val logRecord = LogRecord(
      level = level,
      levelValue = level.value,
      messages = message :: Option(record.getThrown).map(t => TraceLoggableMessage(t)).toList,
      fileName = "",
      className = Option(record.getSourceClassName).orElse(name).getOrElse("java.util.logging"),
      methodName = Option(record.getSourceMethodName),
      line = None,
      column = None
    )
    logger.log(logRecord)
  }

  /** The record's message with its `{0}`, `{1}`, … placeholders filled from its parameters. */
  private def format(record: java.util.logging.LogRecord): String = {
    val message = Option(record.getMessage).getOrElse("")
    Option(record.getParameters).filter(_.nonEmpty) match {
      case Some(parameters) =>
        parameters.zipWithIndex.foldLeft(message) { case (text, (value, index)) =>
          text.replace(s"{$index}", String.valueOf(value))
        }
      case None => message
    }
  }

  private def l2l(level: JLevel): Level = level match {
    case JLevel.FINEST => Level.Trace
    case JLevel.FINER => Level.Trace
    case JLevel.FINE => Level.Trace
    case JLevel.CONFIG => Level.Debug
    case JLevel.INFO => Level.Info
    case JLevel.WARNING => Level.Warn
    case JLevel.SEVERE => Level.Error
    case JLevel.OFF => Level.Trace
    case JLevel.ALL => Level.Fatal
    case _ => Level.Info
  }

  override def flush(): Unit = {}

  override def close(): Unit = {}
}
