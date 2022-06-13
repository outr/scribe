package scribe.message

import scribe.Loggable

trait Message[M] extends LoggableMessage {
  override def value: M
}

//object Message {
//  def static[M: Loggable](value: M): Message[M] = StaticMessage(value)
//  def apply[M: Loggable](value: => M): Message[M] = new LazyMessage[M](() => value)
//  def empty: Message[String] = EmptyMessage
//}