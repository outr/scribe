package scribe.record

import scribe.message.{LoggableMessage, Message}
import scribe.output.{CompositeOutput, LogOutput}
import scribe.{Level, LogRecord, LogRecordCreator}

import java.util.concurrent.ConcurrentLinkedQueue

class LogRecordPool extends LogRecord {
  var level: Level = Level.Info
  var levelValue: Double = level.value
  var messages: List[LoggableMessage] = Nil
  var fileName: String = ""
  var className: String = ""
  var methodName: Option[String] = None
  var line: Option[Int] = None
  var column: Option[Int] = None
  var thread: Thread = Thread.currentThread()
  var data: Map[String, () => Any] = Map.empty
  var timeStamp: Long = 0L

  private var logOutputOption: Option[LogOutput] = None

  override def logOutput: LogOutput = synchronized {
    logOutputOption match {
      case Some(output) => output
      case None =>
        val output = generateLogOutput()
        logOutputOption = Some(output)
        output
    }
  }

  override def copy(level: Level,
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
    this.level = level
    this.levelValue = value
    this.messages = messages
    this.fileName = fileName
    this.className = className
    this.methodName = methodName
    this.line = line
    this.column = column
    this.thread = thread
    this.data = data
    this.timeStamp = timeStamp
    logOutputOption = None

    this
  }

  override def dispose(): Unit = {
    logOutputOption = None
    LogRecordPool.release(this)
  }
}

object LogRecordPool extends LogRecordCreator {
  private val pool = new ConcurrentLinkedQueue[LogRecordPool]

  private def get(): LogRecord = Option(pool.poll()) match {
    case Some(r) => r.asInstanceOf[LogRecord]
    case None => new LogRecordPool
  }

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
                        timeStamp: Long): LogRecord = get().copy(
    level = level,
    value = value,
    messages = messages,
    fileName = fileName,
    className = className,
    methodName = methodName,
    line = line,
    column = column,
    thread = thread,
    data = data,
    timeStamp = timeStamp
  )

  def release(record: LogRecordPool): Unit = pool.add(record)
}