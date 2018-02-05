package scribe.util

import scribe._
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.{Currency, Locale}

object NumberFormatUtil {
  private var map = Map.empty[String, NumberFormatter]

  def apply(i: Int = 1,
            f: Int = 2,
            maxI: Int = 9,
            maxF: Int = 100,
            g: Boolean = true,
            c: Currency = Currency.getInstance(Locale.getDefault),
            rm: RoundingMode = RoundingMode.HALF_UP): NumberFormatter = synchronized {
    val key: String = sfi"$i,$f,$maxI,$maxF,$g,$c,$rm"
    map.get(key) match {
      case Some(nf) => nf
      case None => {
        val nf = new NumberFormatter(i, f, maxI, maxF, g, c, rm)
        map += key -> nf
        nf
      }
    }
  }

  class NumberFormatter(i: Int,
                        f: Int,
                        maxI: Int,
                        maxF: Int,
                        g: Boolean,
                        c: Currency,
                        rm: RoundingMode) {
    private lazy val nf = {
      val nf = NumberFormat.getInstance()
      nf.setGroupingUsed(g)
      nf.setCurrency(c)
      nf.setMaximumFractionDigits(maxF)
      nf.setMinimumFractionDigits(f)
      nf.setMaximumIntegerDigits(maxI)
      nf.setMinimumIntegerDigits(i)
      nf.setParseIntegerOnly(maxF == 0)
      nf.setRoundingMode(rm)
      nf
    }

    def format(l: Long): String = synchronized(nf.format(l))
    def format(d: Double): String = synchronized(nf.format(d))
  }
}