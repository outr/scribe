package scribe

package object rapid extends RapidLoggerSupport {
  implicit class LoggerExtras(val logger: Logger) extends AnyVal {
    def rapid: RapidLoggerSupport = new RapidLoggerWrapper(logger)
  }
}