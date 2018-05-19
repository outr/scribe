import scala.concurrent.ExecutionContext
import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe extends LoggerSupport {
  protected[scribe] var disposables = Set.empty[() => Unit]

  override def log[M](record: LogRecord[M]): Unit = Logger(record.className).log(record)

  def dispose(): Unit = disposables.foreach(d => d())

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger(value.getClass.getName)
  }

  object Execution {
    implicit def global: ExecutionContext = macro Macros.executionContext
  }
}