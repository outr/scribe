package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log(record: LogRecord): Unit

  def log(level: Level, message: => Any): Unit = macro Macros.log
  
  def trace(message: => Any): Unit = macro Macros.trace
  def debug(message: => Any): Unit = macro Macros.debug
  def info(message: => Any): Unit = macro Macros.info
  def warn(message: => Any): Unit = macro Macros.warn
  def error(message: => Any): Unit = macro Macros.error

  def trace(message: => Any, t: => Throwable): Unit = macro Macros.trace2
  def debug(message: => Any, t: => Throwable): Unit = macro Macros.debug2
  def info(message: => Any, t: => Throwable): Unit = macro Macros.info2
  def warn(message: => Any, t: => Throwable): Unit = macro Macros.warn2
  def error(message: => Any, t: => Throwable): Unit = macro Macros.error2
}