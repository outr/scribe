package scribe

import scribe.format.FormatBlock
import scribe.format.FormatBlock.NewLine
import scribe.message.LoggableMessage
import scribe.modify.LogModifier
import scribe.output.{CompositeOutput, EmptyOutput, LogOutput, TextOutput}
import scribe.record.SimpleLogRecord
import scribe.util.Time

import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec

trait LogRecord {
  protected var appliedModifierIds = Set.empty[String]

  final val id: Long = LogRecord.incrementor.incrementAndGet()
  def level: Level
  def levelValue: Double
  def messages: List[LoggableMessage]
  def fileName: String
  def className: String
  def methodName: Option[String]
  def line: Option[Int]
  def column: Option[Int]
  def thread: Thread
  def data: Map[String, () => Any]
  def timeStamp: Long

  def logOutput: LogOutput

  protected def generateLogOutput(): LogOutput = messages match {
    case msg :: Nil => msg.logOutput
    case list => new CompositeOutput(
      list.flatMap { message =>
        List(LogRecord.messageSeparator.format(this), message.logOutput)
      }.drop(1)
    )
  }

  def get(key: String): Option[Any] = data.get(key).map(_())

  def boost(booster: Double => Double): LogRecord = copy(value = booster(levelValue))
  def checkModifierId(id: String, add: Boolean = true): Boolean = id match {
    case "" => false
    case _ if appliedModifierIds.contains(id) => true
    case _ =>
      if (add) appliedModifierIds += id
      false
  }
  def modify(modifier: LogModifier): Option[LogRecord] = if (checkModifierId(modifier.id)) {
    Some(this)
  } else {
    modifier(this)
  }
  @tailrec
  final def modify(modifiers: List[LogModifier]): Option[LogRecord] = if (modifiers.isEmpty) {
    Some(this)
  } else {
    modify(modifiers.head) match {
      case None => None
      case Some(record) => record.modify(modifiers.tail)
    }
  }

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
           timeStamp: Long = timeStamp): LogRecord

  def dispose(): Unit
}

object LogRecord extends LogRecordCreator {
  private val incrementor = new AtomicLong(0L)

  /**
   * The LogRecordCreator to create LogRecords. Defaults to SimpleLogRecord.
   */
  var creator: LogRecordCreator = SimpleLogRecord

  /**
   * The separator between multiple messages for the same LogRecord. Defaults to NewLine.
   */
  var messageSeparator: FormatBlock = NewLine

  override def apply(level: Level,
                     value: Double,
                     messages: List[LoggableMessage],
                     fileName: String,
                     className: String,
                     methodName: Option[String],
                     line: Option[Int],
                     column: Option[Int],
                     thread: Thread = Thread.currentThread(),
                     data: Map[String, () => Any] = Map.empty,
                     timeStamp: Long = Time()): LogRecord = {
    creator(level, value, messages, fileName, className, methodName, line, column, thread, data, timeStamp)
  }

  def simple(message: String,
             fileName: String,
             className: String,
             methodName: Option[String] = None,
             line: Option[Int] = None,
             column: Option[Int] = None,
             level: Level = Level.Info,
             thread: Thread = Thread.currentThread(),
             data: Map[String, () => Any] = Map.empty,
             timeStamp: Long = Time()): LogRecord = {
    apply(
      level = level,
      value = level.value,
      messages = List(message),
      fileName = fileName,
      className = className,
      methodName = methodName,
      line = line,
      column = column,
      thread = thread,
      data = data,
      timeStamp = timeStamp
    )
  }
}