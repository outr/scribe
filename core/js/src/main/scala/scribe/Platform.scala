package scribe

object Platform {
  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false
}