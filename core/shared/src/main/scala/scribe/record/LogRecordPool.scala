package scribe.record

import scribe.LogRecord.throwable2LogOutput
import scribe.output.{EmptyOutput, LogOutput}
import scribe.{Level, LogRecord, LogRecordCreator, Loggable, Message}

import java.util.concurrent.ConcurrentLinkedQueue

class LogRecordPool[M] extends LogRecord[M] {
  var level: Level = Level.Info
  var levelValue: Double = level.value
  var message: Message[M] = Message.empty[M]
  var loggable: Loggable[M] = Loggable.StringLoggable.asInstanceOf[Loggable[M]]
  var throwable: Option[Throwable] = None
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
        val msg = loggable(message.value)
        val output = throwable match {
          case Some(t) => throwable2LogOutput(msg, t)
          case None => msg
        }
        logOutputOption = Some(output)
        output
    }
  }

  override def copy(level: Level,
                    value: Double,
                    message: Message[M],
                    loggable: Loggable[M],
                    throwable: Option[Throwable],
                    fileName: String,
                    className: String,
                    methodName: Option[String],
                    line: Option[Int],
                    column: Option[Int],
                    thread: Thread,
                    data: Map[String, () => Any],
                    timeStamp: Long): LogRecord[M] = {
    this.level = level
    this.levelValue = value
    this.message = message
    this.loggable = loggable
    this.throwable = throwable
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
  private val pool = new ConcurrentLinkedQueue[LogRecordPool[Any]]

  private def get[M](): LogRecord[M] = Option(pool.poll()) match {
    case Some(r) => r.asInstanceOf[LogRecord[M]]
    case None => new LogRecordPool[M]
  }

  override def apply[M](level: Level,
                        value: Double,
                        message: Message[M],
                        loggable: Loggable[M],
                        throwable: Option[Throwable],
                        fileName: String,
                        className: String,
                        methodName: Option[String],
                        line: Option[Int],
                        column: Option[Int],
                        thread: Thread,
                        data: Map[String, () => Any],
                        timeStamp: Long): LogRecord[M] = get[M]().copy(
    level = level,
    value = value,
    message = message,
    loggable = loggable,
    throwable = throwable,
    fileName = fileName,
    className = className,
    methodName = methodName,
    line = line,
    column = column,
    thread = thread,
    data = data,
    timeStamp = timeStamp
  )

  def release[M](record: LogRecordPool[M]): Unit = pool.add(record.asInstanceOf[LogRecordPool[Any]])
}