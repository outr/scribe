package scribe

object EmptyMessage extends Message[String] {
  override def value: String = ""
}