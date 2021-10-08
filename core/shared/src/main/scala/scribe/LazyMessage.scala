package scribe

class LazyMessage[M](function: () => M) extends Message[M] {
  override lazy val value: M = function()
}