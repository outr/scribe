package scribe

import java.time.LocalDateTime
import java.time.temporal.ChronoField
import java.util.TimeZone

class Java8CrossDate(override val milliseconds: Long, ldt: LocalDateTime) extends CrossDate {
  override def hour24: Int = ldt.get(ChronoField.HOUR_OF_DAY)
  override def minuteOfHour: Int = ldt.get(ChronoField.MINUTE_OF_HOUR)
  override def secondOfMinute: Int = ldt.get(ChronoField.SECOND_OF_MINUTE)
  override def milliOfSecond: Int = ldt.get(ChronoField.MILLI_OF_SECOND)
  override def isAM: Boolean = ldt.get(ChronoField.AMPM_OF_DAY) == 0
  override def timeZoneOffset: Int = TimeZone.getDefault.getOffset(milliseconds)
  override def year: Int = ldt.get(ChronoField.YEAR)
  override def month: Int = ldt.get(ChronoField.MONTH_OF_YEAR)
  override def dayOfWeek: Int = ldt.get(ChronoField.DAY_OF_WEEK)
  override def dayOfMonth: Int = ldt.get(ChronoField.DAY_OF_MONTH)
  override def dayOfYear: Int = ldt.get(ChronoField.DAY_OF_YEAR)
}
