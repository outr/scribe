import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe extends LoggerSupport {
  protected[scribe] var disposables = Set.empty[() => Unit]

  override def log(record: LogRecord): Unit = Logger.byName(record.className).log(record)

  def dispose(): Unit = disposables.foreach(d => d())

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger.byName(value.getClass.getSimpleName)
    def updateLogger(modifier: Logger => Logger): Logger = Logger.update(value.getClass.getSimpleName)(modifier)
  }
}