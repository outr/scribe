package scribe

class LazyMessage[M](function: () => M) {
  lazy val value: M = function()
}