import scala.language.experimental.macros

package object scribe extends LoggerSupport {
  override val name: Option[String] = Some("scribe")
  override val parentName: Option[String] = Some(Logger.rootName)
  override val multiplier: Double = 1.0

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger.byName(value.getClass.getSimpleName)
  }
}