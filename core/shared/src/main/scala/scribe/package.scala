import scribe.implicits.{DoubleImplicits, LongImplicits}

import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe extends LoggerSupport {
  protected[scribe] var disposables = Set.empty[() => Unit]

  override def log(record: LogRecord): Unit = Logger.byName(record.className).log(record)

  def dispose(): Unit = disposables.foreach(d => d())

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger.byName(value.getClass.getName)

    def updateLogger(modifier: Logger => Logger): Logger = Logger.update(value.getClass.getName)(modifier)
  }

  implicit def long2Implicits(l: Long): LongImplicits = new LongImplicits(l)
  implicit def double2Implicits(d: Double): DoubleImplicits = new DoubleImplicits(d)


  implicit class SFIInterpolator(val sc: StringContext) extends AnyVal {
    def sfi(args: Any*): String = macro SFIMacros.sfiImpl
  }
}