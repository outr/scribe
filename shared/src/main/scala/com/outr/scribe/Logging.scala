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

  /**
    * The logger for this class. Though it can be replaced (being this is a var), it is recommended you use the
    * `updateLogger` method instead to properly retain existing handlers.
    */
  @transient protected var logger: Logger = Logger(loggerName)

  /**
    * Updates the current logger for this class. The supplied function receives the current logger instance and should
    * return the instance intended to replace it. Upon completion of the function the handlers that currently exist on
    * the previous logger will be added to the new logger.
    */
  protected def updateLogger(f: Logger => Logger): Unit = {
    val original = logger
    val updated = f(original)
    if (original.handlers.nonEmpty) {
      updated.handlers ++= original.handlers
    }
    logger = updated
  }
}
