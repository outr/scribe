package scribe.date

import scribe._
import scribe.util.NumberFormatUtil._

trait CrossDate {
  def milliseconds: Long
  def hour24: Int
  def minuteOfHour: Int
  def secondOfMinute: Int
  def milliOfSecond: Int
  def isAM: Boolean
  def timeZoneOffset: Int

  def year: Int
  def month: Int
  def dayOfWeek: Int
  def dayOfMonth: Int
  def dayOfYear: Int

  def hour12: Int = hour24 match {
    case i if i > 11 => i - 11
    case i => i
  }
  def isPM: Boolean = !isAM
  def secondsOfEpoch: Long = milliseconds / 1000L

  // Time short-hand
  def H: String = int(hour24, 2)
  def I: String = int(hour12, 2)
  def k: String = hour24.toString
  def l: String = hour12.toString
  def M: String = int(minuteOfHour, 2)
  def S: String = int(secondOfMinute, 2)
  def L: String = int(milliOfSecond, 3)
  def p: String = if (isAM) "am" else "pm"
  def P: String = if (isAM) "AM" else "PM"
  def z: String = int(timeZoneOffset, 4)
  def s: String = secondsOfEpoch.toString

  // Date short-hand
  def B: String = CrossDate.Month.Long(month)
  def b: String = CrossDate.Month.Short(month)
  def h: String = CrossDate.Month.Short(month)
  def A: String = CrossDate.Week.Long(dayOfWeek)
  def a: String = CrossDate.Week.Short(dayOfWeek)
  def Y: String = year.toString
  def y: String = (year % 100).toString
  def j: String = (dayOfYear + 1).toString
  def m: String = int(month + 1, 2)
  def d: String = int(dayOfMonth + 1, 2)
  def e: String = (dayOfMonth + 1).toString
  def R: String = sfi"$H:$M"
  def T: String = sfi"$H:$M:$S"
  def r: String = sfi"$I:$M:$S:$p"
  def D: String = sfi"$m/$d/$y"
  def F: String = sfi"$Y-$m-$d"
  def c: String = sfi"$a $b $d $T $z $Y"
}

object CrossDate {
  private val cache = new ThreadLocal[CrossDate]

  def apply(l: Long): CrossDate = Option(cache.get()) match {
    case Some(d) if d.milliseconds == l => d
    case _ => {
      val d = Platform.createDate(l)
      cache.set(d)
      d
    }
  }

  object Week {
    val Long: Vector[String] = Vector(
      "Sunday",
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday"
    )
    val Short: Vector[String] = Vector(
      "Sun",
      "Mon",
      "Tues",
      "Wed",
      "Thurs",
      "Fri",
      "Sat"
    )
  }
  object Month {
    val Long: Vector[String] = Vector(
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December"
    )
    val Short: Vector[String] = Vector(
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec"
    )
  }
}