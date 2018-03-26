package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log[M](record: LogRecord[M]): Unit

  def log[M](level: Level, message: M, throwable: Option[Throwable])
            (implicit stringify: M => String): Unit = macro Macros.log[M]
  
  def trace[M](message: M)(implicit stringify: M => String): Unit = macro Macros.autoLevel[M]
  def debug[M](message: M)(implicit stringify: M => String): Unit = macro Macros.autoLevel[M]
  def info[M](message: M)(implicit stringify: M => String): Unit = macro Macros.autoLevel[M]
  def warn[M](message: M)(implicit stringify: M => String): Unit = macro Macros.autoLevel[M]
  def error[M](message: M)(implicit stringify: M => String): Unit = macro Macros.autoLevel[M]

  def trace[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.autoLevel2[M]
  def debug[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.autoLevel2[M]
  def info[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.autoLevel2[M]
  def warn[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.autoLevel2[M]
  def error[M](message: M, t: Throwable)(implicit stringify: M => String): Unit = macro Macros.autoLevel2[M]
}