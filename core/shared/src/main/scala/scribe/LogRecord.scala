package scribe

import scribe.format.FormatBlock
import scribe.format.FormatBlock.NewLine
import scribe.message.LoggableMessage
import scribe.modify.LogModifier
import scribe.output.{CompositeOutput, LogOutput}
import scribe.util.Time

import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec

case class LogRecord(level: Level,
                     levelValue: Double,
                     messages: List[LoggableMessage],
                     fileName: String,
                     className: String,
                     methodName: Option[String],
                     line: Option[Int],
                     column: Option[Int],
                     thread: Thread = Thread.currentThread(),
                     data: Map[String, () => Any] = Map.empty,
                     timeStamp: Long = Time()) {
  protected var appliedModifierIds = Set.empty[String]

  final val id: Long = LogRecord.incrementor.incrementAndGet()

  lazy val logOutput: LogOutput = generateLogOutput()

  protected def generateLogOutput(): LogOutput = messages match {
    case msg :: Nil => msg.logOutput
    case list => new CompositeOutput(
      list.flatMap { message =>
        List(LogRecord.messageSeparator.format(this), message.logOutput)
      }.drop(1)
    )
  }

  def withFeatures(features: LogFeature*): LogRecord = features.foldLeft(this)((record, feature) => feature(record))

  def withMessages(messages: LoggableMessage*): LogRecord = copy(messages = this.messages ::: messages.toList)

  def get(key: String): Option[Any] = data.get(key).map(_())
  def update(key: String, value: () => Any): LogRecord = copy(data = data + (key -> value))

  def boost(booster: Double => Double): LogRecord = copy(levelValue = booster(levelValue))
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
}

object LogRecord {
  private val incrementor = new AtomicLong(0L)

  /**
   * The separator between multiple messages for the same LogRecord. Defaults to NewLine.
   */
  var messageSeparator: FormatBlock = NewLine

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
      levelValue = level.value,
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