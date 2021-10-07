import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe extends LoggerSupport {
  lazy val lineSeparator: String = System.getProperty("line.separator")

  protected[scribe] var disposables = Set.empty[() => Unit]

  override def log[M](record: LogRecord[M]): Unit = Logger(record.className).log(record)

  override def includes(level: Level): Boolean = true

  def dispose(): Unit = disposables.foreach(d => d())

  implicit def level2Double(level: Level): Double = level.value

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger(value.getClass.getName)
  }
}