package scribe.record

import scribe.LogRecord.throwable2LogOutput
import scribe.message.{LoggableMessage, Message}
import scribe.{Level, LogRecord, LogRecordCreator, Loggable}
import scribe.output.{CompositeOutput, LogOutput}

class SimpleLogRecord[M](val level: Level,
                         val levelValue: Double,
                         val message: Message[M],
                         val additionalMessages: List[LoggableMessage],
                         val fileName: String,
                         val className: String,
                         val methodName: Option[String],
                         val line: Option[Int],
                         val column: Option[Int],
                         val thread: Thread,
                         val data: Map[String, () => Any],
                         val timeStamp: Long) extends LogRecord[M] {
  override lazy val logOutput: LogOutput = {
    val msg = message.logOutput
    additionalMessages match {
      case Nil => msg
      case list => new CompositeOutput(msg :: LogOutput.NewLine :: list.map(_.logOutput))
    }
  }

  def copy(level: Level = level,
           value: Double = levelValue,
           message: Message[M] = message,
           additionalMessages: List[LoggableMessage] = additionalMessages,
           fileName: String = fileName,
           className: String = className,
           methodName: Option[String] = methodName,
           line: Option[Int] = line,
           column: Option[Int] = column,
           thread: Thread = thread,
           data: Map[String, () => Any] = data,
           timeStamp: Long = timeStamp): LogRecord[M] = {
    val r = new SimpleLogRecord(level, value, message, additionalMessages, fileName, className, methodName, line, column, thread, data, timeStamp)
    r.appliedModifierIds = this.appliedModifierIds
    r
  }

  override def dispose(): Unit = {}
}

object SimpleLogRecord extends LogRecordCreator {
  override def apply[M](level: Level,
                        value: Double,
                        message: Message[M],
                        additionalMessages: List[LoggableMessage],
                        fileName: String,
                        className: String,
                        methodName: Option[String],
                        line: Option[Int],
                        column: Option[Int],
                        thread: Thread,
                        data: Map[String, () => Any],
                        timeStamp: Long): LogRecord[M] = {
    new SimpleLogRecord(level, value, message, additionalMessages, fileName, className, methodName, line, column, thread, data, timeStamp)
  }
}