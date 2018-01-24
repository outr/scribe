package scribe

trait LogRecord {
  def level: Level
  def value: Double
  def message: String
  def className: String
  def methodName: Option[String]
  def lineNumber: Option[Int]
  def thread: Thread
  def timeStamp: Long

  def boost(booster: Double => Double): LogRecord = copy(value = booster(value))

  def copy(level: Level = level,
           value: Double = value,
           message: String = message,
           className: String = className,
           methodName: Option[String] = methodName,
           lineNumber: Option[Int] = lineNumber,
           thread: Thread = thread,
           timeStamp: Long = timeStamp): LogRecord

  def dispose(): Unit
}

object LogRecord {
  def apply(level: Level,
            value: Double,
            message: String,
            className: String,
            methodName: Option[String],
            lineNumber: Option[Int],
            thread: Thread,
            timeStamp: Long): LogRecord = {
    SimpleLogRecord(level, value, message, className, methodName, lineNumber, thread, timeStamp)
  }

  case class SimpleLogRecord(level: Level,
                             value: Double,
                             message: String,
                             className: String,
                             methodName: Option[String],
                             lineNumber: Option[Int],
                             thread: Thread,
                             timeStamp: Long) extends LogRecord {
    def copy(level: Level = level,
             value: Double = value,
             message: String = message,
             className: String = className,
             methodName: Option[String] = methodName,
             lineNumber: Option[Int] = lineNumber,
             thread: Thread = thread,
             timeStamp: Long = timeStamp): LogRecord = {
      SimpleLogRecord(level, value, message, className, methodName, lineNumber, thread, timeStamp)
    }

    override def dispose(): Unit = {}
  }
}