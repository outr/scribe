package scribe

import scribe.message.{LoggableMessage, Message}
import scribe.util.Time

trait LogRecordCreator {
  def apply[M](level: Level,
               value: Double,
               message: Message[M],
               additionalMessages: List[LoggableMessage],
               fileName: String,
               className: String,
               methodName: Option[String],
               line: Option[Int],
               column: Option[Int],
               thread: Thread = Thread.currentThread(),
               data: Map[String, () => Any] = Map.empty,
               timeStamp: Long = Time()): LogRecord[M]
}