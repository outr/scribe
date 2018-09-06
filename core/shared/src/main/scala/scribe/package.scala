import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe extends LoggerSupport {
  lazy val lineSeparator: String = System.getProperty("line.separator")

  protected[scribe] var disposables = Set.empty[() => Unit]

  override def log[M](record: LogRecord[M]): Unit = Logger(record.className).log(record)

  def dispose(): Unit = disposables.foreach(d => d())

  implicit def level2Double(level: Level): Double = level.value

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger(value.getClass.getName)
  }

  /**
    * Updates `Throwables` fired within the supplied function to include positional information tying back up the
    * asynchronous chain. This should only be necessary if you have no control over the creation of `Future`s in your
    * code. Ideally, just use `scribe.future` instead.
    *
    * Use this as a function wrapper to fix `Throwable` instances. This must be coupled with:
    *
    * `import scribe.Execution.global`
    *
    * Or there will be no stack to inject.
    *
    * @param f the functional that may throw a `Throwable`
    * @tparam Return the return type of the wrapped function
    */
  def async[Return](f: => Return): Return = macro Macros.async[Return]

  /**
    * Convenience method to create a `Future` with the Scribe `ExecutionContext` to properly track tracing up the chain
    * for asynchronous operations. When using this, no other operation should be necessary. However, if you map to other
    * future operations that require an `ExecutionContext`, make sure to use `import scribe.Execution.global` instead of
    * the default `global` `ExecutionContext` or the stack data will be lost.
    *
    * @param f the function to run in a new `Future`
    * @tparam Return the return type for the code
    */
  def future[Return](f: => Return): Future[Return] = macro Macros.future[Return]

  object Execution {
    implicit def global: ExecutionContext = macro Macros.executionContext
  }
}