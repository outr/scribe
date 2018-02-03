import java.time.{Instant, LocalDateTime, ZoneId}

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

  implicit class SFInterpolator(val sc: StringContext) extends AnyVal {
    def sf(args: Any*): String = macro Macros.sf
  }

  object SFInterpolator {
    private val _state = new ThreadLocal[Option[SFState]] {
      override def initialValue(): Option[SFState] = None
    }
    private def state: SFState = _state.get().getOrElse(throw new RuntimeException("Cannot be called outside a transaction!"))

    def date(l: Long): LocalDateTime = {
      val s = state
      s.dateCache.get(l) match {
        case Some(ldt) => ldt
        case None => {
          val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault())
          s.dateCache += l -> ldt
          ldt
        }
      }
    }

    def transaction[R](f: => R): R = {
      _state.set(Some(new SFState()))
      try {
        f
      } finally {
        _state.remove()
      }
    }

    class SFState {
      var dateCache: Map[Long, LocalDateTime] = Map.empty[Long, LocalDateTime]
    }
  }
}