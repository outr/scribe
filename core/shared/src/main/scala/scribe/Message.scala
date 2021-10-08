package scribe

trait Message[M] {
  def value: M
}