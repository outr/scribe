package scribe

import scribe.modify.{LevelFilter, LogModifier}
import scribe.util.Time

trait LogSupport[L <: LogSupport[L]] {
  def modifiers: List[LogModifier]

  def setModifiers(modifiers: List[LogModifier]): L

  def clearModifiers(): L = setModifiers(Nil)

  final def withModifier(modifier: LogModifier): L = setModifiers((modifiers.filterNot(_.id == modifier.id) ::: List(modifier)).sorted)
  final def withoutModifier(modifier: LogModifier): L = setModifiers(modifiers.filterNot(_.id == modifier.id))

  def withMinimumLevel(level: Level): L = withModifier(LevelFilter >= level)

  def includes(level: Level): Boolean = {
    modifiers.find(_.id == LevelFilter.Id).map(_.asInstanceOf[LevelFilter]).forall(_.accepts(level.value))
  }

  def log[M](record: LogRecord[M]): Unit

  def logDirect[M](level: Level,
                   message: => M,
                   throwable: Option[Throwable] = None,
                   fileName: String = "",
                   className: String = "",
                   methodName: Option[String] = None,
                   line: Option[Int] = None,
                   column: Option[Int] = None,
                   thread: Thread = Thread.currentThread(),
                   timeStamp: Long = Time())
                  (implicit loggable: Loggable[M]): Unit = {
    log[M](LogRecord[M](
      level = level,
      value = level.value,
      message = new LazyMessage[M](() => message),
      loggable = loggable,
      throwable = throwable,
      fileName = fileName,
      className = className,
      methodName = methodName,
      line = line,
      column = column,
      thread = thread,
      timeStamp = timeStamp
    ))
  }
}