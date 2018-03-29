package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log[M](record: LogRecord[M]): Unit

  def log[M](level: Level, message: M, throwable: Option[Throwable])
            (implicit loggable: Loggable[M]): Unit = macro Macros.log[M]
  
  def trace[M : Loggable](message: M): Unit = macro Macros.autoLevel[M]
  def debug[M : Loggable](message: M): Unit = macro Macros.autoLevel[M]
  def info[M : Loggable](message: M): Unit = macro Macros.autoLevel[M]
  def warn[M : Loggable](message: M): Unit = macro Macros.autoLevel[M]
  def error[M : Loggable](message: M): Unit = macro Macros.autoLevel[M]

  def trace[M : Loggable](message: M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def debug[M : Loggable](message: M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def info[M : Loggable](message: M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def warn[M : Loggable](message: M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def error[M : Loggable](message: M, t: Throwable): Unit = macro Macros.autoLevel2[M]
}
