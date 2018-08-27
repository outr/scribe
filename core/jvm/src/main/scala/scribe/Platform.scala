package scribe

object Platform {
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false
}