package scribe

trait LogRecord {
  def level: Level
  def value: Double
  def messageFunction: () => Any
  def stringify: Any => String
  def className: String
  def methodName: Option[String]
  def lineNumber: Option[Int]
  def thread: Thread
  def timeStamp: Long

  lazy val message: String = stringify(messageFunction())

  def boost(booster: Double => Double): LogRecord = copy(value = booster(value))

  def copy(level: Level = level,
           value: Double = value,
           messageFunction: () => Any = messageFunction,
           stringify: Any => String = stringify,
           className: String = className,
           methodName: Option[String] = methodName,
           lineNumber: Option[Int] = lineNumber,
           thread: Thread = thread,
           timeStamp: Long = timeStamp): LogRecord

  def dispose(): Unit
}

object LogRecord {
  object Stringify {
    val Default: Any => String = (v: Any) => String.valueOf(v)
  }

  def apply(level: Level,
            value: Double,
            messageFunction: () => Any,
            stringify: Any => String,
            className: String,
            methodName: Option[String],
            lineNumber: Option[Int],
            thread: Thread = Thread.currentThread(),
            timeStamp: Long = System.currentTimeMillis()): LogRecord = {
    SimpleLogRecord(level, value, messageFunction, stringify, className, methodName, lineNumber, thread, timeStamp)
  }

  case class SimpleLogRecord(level: Level,
                             value: Double,
                             messageFunction: () => Any,
                             stringify: Any => String,
                             className: String,
                             methodName: Option[String],
                             lineNumber: Option[Int],
                             thread: Thread,
                             timeStamp: Long) extends LogRecord {
    def copy(level: Level = level,
             value: Double = value,
             messageFunction: () => Any = messageFunction,
             stringify: Any => String = stringify,
             className: String = className,
             methodName: Option[String] = methodName,
             lineNumber: Option[Int] = lineNumber,
             thread: Thread = thread,
             timeStamp: Long = timeStamp): LogRecord = {
      SimpleLogRecord(level, value, messageFunction, stringify, className, methodName, lineNumber, thread, timeStamp)
    }

    override def dispose(): Unit = {}
  }
}