package scribe.implicits

import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.temporal.{ChronoField, TemporalField}
import java.util.{Currency, Locale, TimeZone}

import scribe._
import scribe.util.{LastLocalDateTime, NumberFormatUtil}

class LongImplicits(val l: Long) extends AnyVal {
  private def ldt: LocalDateTime = LastLocalDateTime(l)

  private def chrono(f: TemporalField, digits: Int = 0): String = if (digits == 0) {
    ldt.get(f).toString
  } else {
    intFormat(ldt.get(f), digits)
  }

  // Time
  def tH: String = chrono(ChronoField.HOUR_OF_DAY, 2)
  def tI: String = chrono(ChronoField.CLOCK_HOUR_OF_AMPM, 2)
  def tk: String = chrono(ChronoField.HOUR_OF_DAY)
  def tl: String = chrono(ChronoField.HOUR_OF_AMPM)
  def tM: String = chrono(ChronoField.MINUTE_OF_HOUR, 2)
  def tS: String = chrono(ChronoField.SECOND_OF_MINUTE, 2)
  def tL: String = chrono(ChronoField.MILLI_OF_SECOND, 3)
  def tN: String = chrono(ChronoField.NANO_OF_SECOND, 2)
  def tp: String = if (ldt.get(ChronoField.AMPM_OF_DAY) == 0) "am" else "pm"
  def Tp: String = if (ldt.get(ChronoField.AMPM_OF_DAY) == 0) "AM" else "PM"
  def tz: String = intFormat(TimeZone.getDefault.getOffset(l), 4)
  def tZ: String = TimeZone.getDefault.getDisplayName
  def TZ: String = tZ.toUpperCase
  def ts: String = chrono(ChronoField.INSTANT_SECONDS, 2)

  // Date
  def tB: String = ldt.getMonth.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault)
  def TB: String = tB.toUpperCase
  def tb: String = ldt.getMonth.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault)
  def Tb: String = tb.toUpperCase
  def th: String = ldt.getMonth.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault)
  def Th: String = th.toUpperCase
  def tA: String = ldt.getDayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault)
  def TA: String = tA.toUpperCase
  def ta: String = ldt.getDayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault)
  def Ta: String = ta.toUpperCase
  def tC: String = chrono(ChronoField.YEAR_OF_ERA, 2)
  def tY: String = chrono(ChronoField.YEAR, 4)
  def ty: String = (ldt.get(ChronoField.YEAR) % 100).toString
  def tj: String = chrono(ChronoField.DAY_OF_YEAR)
  def tm: String = chrono(ChronoField.MONTH_OF_YEAR, 2)
  def td: String = chrono(ChronoField.DAY_OF_MONTH, 2)
  def te: String = chrono(ChronoField.DAY_OF_MONTH)
  def tR: String = sfi"$tH:$tM"
  def tT: String = sfi"$tH:$tM:$tS"
  def tr: String = sfi"$tI:$tM:$tS $Tp"
  def tD: String = sfi"$tm/$td/$ty"
  def tF: String = sfi"$tY-$tm-$td"
  def tc: String = sfi"$ta $tb $td $tT $tZ $tY"

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

  private def intFormat(i: Int, digits: Int): String = {
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
}