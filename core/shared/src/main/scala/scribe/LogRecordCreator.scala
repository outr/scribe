package scribe

import scribe.message.{LoggableMessage, Message}
import scribe.util.Time

trait LogRecordCreator {
  def apply(level: Level,
            value: Double,
            messages: List[LoggableMessage],
            fileName: String,
            className: String,
            methodName: Option[String],
            line: Option[Int],
            column: Option[Int],
            thread: Thread = Thread.currentThread(),
            data: Map[String, () => Any] = Map.empty,
            timeStamp: Long = Time()): LogRecord
}