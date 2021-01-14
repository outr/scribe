package scribe.file.path

import scribe.file.FileWriter
import perfolation._

trait FileNamePart {
  def current(timeStamp: Long): String

  def regex: String

  def before(writer: FileWriter): Unit = {}

  def after(writer: FileWriter): Unit = {}
}

object FileNamePart {
  case class Static(s: String) extends FileNamePart {
    override def current(timeStamp: Long): String = s

    override def regex: String = s
  }

  case object Year extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.year.toString

    override def regex: String = "\\d{4}"
  }

  case object Month extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.m

    override def regex: String = "\\d{2}"
  }

  case object Day extends FileNamePart {
    override def current(timeStamp: Long): String = timeStamp.t.d

    override def regex: String = "\\d{2}"
  }
}