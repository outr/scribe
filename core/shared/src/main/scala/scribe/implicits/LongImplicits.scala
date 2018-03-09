package scribe.implicits

import java.math.RoundingMode
import java.util.{Currency, Locale}

import scribe.CrossDate
import scribe.util.NumberFormatUtil

class LongImplicits(val l: Long) extends AnyVal {
  def t: CrossDate = CrossDate(l)

  // Number format
  def f(i: Int = 1,
        f: Int = 2,
        maxI: Int = 9,
        maxF: Int = 100,
        g: Boolean = true,
        c: Currency = Currency.getInstance(Locale.getDefault),
        rm: RoundingMode = RoundingMode.HALF_UP): String = {
    NumberFormatUtil(i, f, maxI, maxF, g, c, rm).format(l)
  }
}