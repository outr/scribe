package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log(record: LogRecord): Unit

  def log(level: Level, message: String): Unit = macro Macros.log
  
  def trace(message: String): Unit = macro Macros.trace
  def debug(message: String): Unit = macro Macros.debug
  def info(message: String): Unit = macro Macros.info
  def warn(message: String): Unit = macro Macros.warn
  def error(message: String): Unit = macro Macros.error

  def trace(t: Throwable): Unit = macro Macros.traceThrowable
  def debug(t: Throwable): Unit = macro Macros.debugThrowable
  def info(t: Throwable): Unit = macro Macros.infoThrowable
  def warn(t: Throwable): Unit = macro Macros.warnThrowable
  def error(t: Throwable): Unit = macro Macros.errorThrowable

  def trace(message: String, t: Throwable): Unit = macro Macros.trace2
  def debug(message: String, t: Throwable): Unit = macro Macros.debug2
  def info(message: String, t: Throwable): Unit = macro Macros.info2
  def warn(message: String, t: Throwable): Unit = macro Macros.warn2
  def error(message: String, t: Throwable): Unit = macro Macros.error2
}