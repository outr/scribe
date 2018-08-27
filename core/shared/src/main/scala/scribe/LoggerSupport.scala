package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def log[M](record: LogRecord[M]): Unit

  def log[M](level: Level, message: => M, throwable: Option[Throwable])
            (implicit loggable: Loggable[M]): Unit = macro Macros.log[M]

  def trace(): Unit = macro Macros.autoLevel0
  def debug(): Unit = macro Macros.autoLevel0
  def info(): Unit = macro Macros.autoLevel0
  def warn(): Unit = macro Macros.autoLevel0
  def error(): Unit = macro Macros.autoLevel0

  def trace[M: Loggable](message: => M): Unit = macro Macros.autoLevel1[M]
  def debug[M: Loggable](message: => M): Unit = macro Macros.autoLevel1[M]
  def info[M: Loggable](message: => M): Unit = macro Macros.autoLevel1[M]
  def warn[M: Loggable](message: => M): Unit = macro Macros.autoLevel1[M]
  def error[M: Loggable](message: => M): Unit = macro Macros.autoLevel1[M]

  def trace[M : Loggable](message: => M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def debug[M : Loggable](message: => M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def info[M : Loggable](message: => M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def warn[M : Loggable](message: => M, t: Throwable): Unit = macro Macros.autoLevel2[M]
  def error[M : Loggable](message: => M, t: Throwable): Unit = macro Macros.autoLevel2[M]
}