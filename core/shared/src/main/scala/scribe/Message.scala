package scribe

trait Message[M] {
  def value: M
}

object Message {
  def static[M](value: M): Message[M] = StaticMessage[M](value)
  def apply[M](value: => M): Message[M] = new LazyMessage[M](() => value)
  def empty[M]: Message[M] = EmptyMessage.asInstanceOf[Message[M]]
}