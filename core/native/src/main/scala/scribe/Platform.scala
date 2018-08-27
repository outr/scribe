package scribe

object Platform {
  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true
}
