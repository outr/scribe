package scribe

/**
  * Logging is a mix-in to conveniently add logging support to any class or object.
  */
trait Logging {
  /**
    * Override this to change the name of the underlying logger.
    *
    * Defaults to class name with package
    */
  protected def loggerName: String = getClass.getName

  /**
    * The logger for this class.
    */
  protected def logger: Logger = Logger.byName(loggerName)
}
