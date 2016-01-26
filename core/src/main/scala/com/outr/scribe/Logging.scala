package com.outr.scribe

/**
  * Logging is a mix-in to conveniently add logging support to any class or object.
  */
trait Logging {
  /**
    * Override this to change the name of the underlying logger.
    *
    * Defaults to class name with package
    */
  protected def loggerName = getClass.getName

  var logger: Logger = Logger(loggerName)
  def updateLogger(f: Logger => Logger): Unit = {
    val original = logger
    val updated = f(original)
    if (original.handlers.nonEmpty) {
      updated.handlers ++= original.handlers
    }
    logger = updated
  }
}
