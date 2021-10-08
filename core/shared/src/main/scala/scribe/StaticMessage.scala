package scribe

case class StaticMessage[M](value: M) extends Message[M]