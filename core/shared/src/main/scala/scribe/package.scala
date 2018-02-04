import java.text.DecimalFormat
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

  implicit class SFIInterpolator(val sc: StringContext) extends AnyVal {
    def sfi(args: Any*): String = macro SFIMacros.sfiImpl
  }

  implicit class SFInterpolator(val sc: StringContext) extends AnyVal {
    def sf(args: Any*): String = macro SFMacros.sf
  }

  object SFInterpolator {
    private val _state = new ThreadLocal[Option[SFState]] {
      override def initialValue(): Option[SFState] = None
    }
    private def state: SFState = _state.get().getOrElse(throw new RuntimeException("Cannot be called outside a transaction!"))

    def intFormat(i: Int, digits: Int): String = {
      val s = i.toString
      val padTo = digits - s.length
      if (padTo <= 0) {
        s
      } else if (padTo == 1) {
        "0".concat(s)
      } else if (padTo == 2) {
        "00".concat(s)
      } else if (padTo == 3) {
        "000".concat(s)
      } else if (padTo == 4) {
        "0000".concat(s)
      } else {
        throw new RuntimeException(s"intFormat padding not available for $digits!")
      }
    }

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

    def dec(d: Double, pattern: String): String = {
      val s = state
      s.decCache.get(pattern) match {
        case Some(df) => df.format(d)
        case None => {
          val df = new DecimalFormat(pattern)
          s.decCache += pattern -> df
          df.format(d)
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
      var decCache: Map[String, DecimalFormat] = Map.empty[String, DecimalFormat]
    }
  }
}