package scribe.record

import scribe.message.{LoggableMessage, Message}
import scribe.{Level, LogRecord, LogRecordCreator}
import scribe.output.{CompositeOutput, LogOutput}

class SimpleLogRecord(val level: Level,
                         val levelValue: Double,
                         val messages: List[LoggableMessage],
                         val fileName: String,
                         val className: String,
                         val methodName: Option[String],
                         val line: Option[Int],
                         val column: Option[Int],
                         val thread: Thread,
                         val data: Map[String, () => Any],
                         val timeStamp: Long) extends LogRecord {
  override lazy val logOutput: LogOutput = generateLogOutput()

  def copy(level: Level = level,
           value: Double = levelValue,
           messages: List[LoggableMessage] = messages,
           fileName: String = fileName,
           className: String = className,
           methodName: Option[String] = methodName,
           line: Option[Int] = line,
           column: Option[Int] = column,
           thread: Thread = thread,
           data: Map[String, () => Any] = data,
           timeStamp: Long = timeStamp): LogRecord = {
    val r = new SimpleLogRecord(level, value, messages, fileName, className, methodName, line, column, thread, data, timeStamp)
    r.appliedModifierIds = this.appliedModifierIds
    r
  }

  override def dispose(): Unit = {}
}

object SimpleLogRecord extends LogRecordCreator {
  override def apply(level: Level,
                        value: Double,
                        messages: List[LoggableMessage],
                        fileName: String,
                        className: String,
                        methodName: Option[String],
                        line: Option[Int],
                        column: Option[Int],
                        thread: Thread,
                        data: Map[String, () => Any],
                        timeStamp: Long): LogRecord = {
    new SimpleLogRecord(level, value, messages, fileName, className, methodName, line, column, thread, data, timeStamp)
  }
}