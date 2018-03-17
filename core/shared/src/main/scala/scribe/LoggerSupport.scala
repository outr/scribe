package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log[M](record: LogRecord[M]): Unit

  def log[M](level: Level, message: M, throwable: Option[Throwable])
            (implicit stringify: M => String): Unit = macro Macros.log[M]
  
  def trace[M](message: M)(implicit stringify: M => String): Unit = macro Macros.trace[M]
  def debug[M](message: M)(implicit stringify: M => String): Unit = macro Macros.debug[M]
  def info[M](message: M)(implicit stringify: M => String): Unit = macro Macros.info[M]
  def warn[M](message: M)(implicit stringify: M => String): Unit = macro Macros.warn[M]
  def error[M](message: M)(implicit stringify: M => String): Unit = macro Macros.error[M]

  def trace[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.trace2[M]
  def debug[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.debug2[M]
  def info[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.info2[M]
  def warn[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.warn2[M]
  def error[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.error2[M]
}