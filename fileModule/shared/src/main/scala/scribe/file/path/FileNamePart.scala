package scribe.file.path

import scribe.file.FileWriter
import perfolation._

import java.util.Calendar

trait FileNamePart {
  def current(timeStamp: Long): String

  def regex: String

  def before(writer: FileWriter): Unit = {}

  def after(writer: FileWriter): Unit = {}

  def nextValidation(timeStamp: Long): Option[Long] = None
}

object FileNamePart {
  case class Static(s: String) extends FileNamePart {
    override def current(timeStamp: Long): String = s

    override def regex: String = s
  }

  case object Year extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.year.toString

    override def regex: String = "\\d{4}"

    override def nextValidation(timeStamp: Long): Option[Long] = {
      val c = Calendar.getInstance()
      c.setTimeInMillis(timeStamp)
      c.add(Calendar.YEAR, 1)
      c.set(Calendar.DAY_OF_YEAR, 1)
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      Some(c.getTimeInMillis)
    }
  }

  case object Month extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.m

    override def regex: String = "\\d{2}"

    override def nextValidation(timeStamp: Long): Option[Long] = {
      val c = Calendar.getInstance()
      c.setTimeInMillis(timeStamp)
      c.add(Calendar.MONTH, 1)
      c.set(Calendar.DAY_OF_MONTH, 1)
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      Some(c.getTimeInMillis)
    }
  }

  case object Day extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.d

    override def regex: String = "\\d{2}"

    override def nextValidation(timeStamp: Long): Option[Long] = {
      val c = Calendar.getInstance()
      c.setTimeInMillis(timeStamp)
      c.add(Calendar.DAY_OF_MONTH, 1)
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      Some(c.getTimeInMillis)
    }
  }

  case object Hour extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.H

    override def regex: String = "\\d{2}"

    override def nextValidation(timeStamp: Long): Option[Long] = {
      val c = Calendar.getInstance()
      c.setTimeInMillis(timeStamp)
      c.add(Calendar.HOUR_OF_DAY, 1)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      Some(c.getTimeInMillis)
    }
  }

  case object Minute extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.M

    override def regex: String = "\\d{2}"

    override def nextValidation(timeStamp: Long): Option[Long] = {
      val c = Calendar.getInstance()
      c.setTimeInMillis(timeStamp)
      c.add(Calendar.MINUTE, 1)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      Some(c.getTimeInMillis)
    }
  }

  case object Second extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.S

    override def regex: String = "\\d{2}"

    override def nextValidation(timeStamp: Long): Option[Long] = {
      val c = Calendar.getInstance()
      c.setTimeInMillis(timeStamp)
      c.add(Calendar.SECOND, 1)
      c.set(Calendar.MILLISECOND, 0)
      Some(c.getTimeInMillis)
    }
  }
}