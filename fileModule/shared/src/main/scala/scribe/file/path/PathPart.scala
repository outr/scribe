package scribe.file.path

import scribe.util.Time

import java.nio.file.{Files, Path, Paths}
import scribe.file.{FileWriter, string2FileNamePart}

trait PathPart {
  def current(previous: String, timeStamp: Long): String

  def all(previous: String): Iterator[String]

  def before(writer: FileWriter): Unit = {}
  def after(writer: FileWriter): Unit = {}
}

object PathPart {
  case object Root extends PathPart {
    override def current(previous: String, timeStamp: Long): String = "/"

    override def all(previous: String): Iterator[String] = Iterator("/")
  }

  case class SetPath(path: String) extends PathPart {
    override def current(previous: String, timeStamp: Long): String = path

    override def all(previous: String): Iterator[String] = Iterator(path)
  }

  case class FileName(parts: List[FileNamePart]) extends PathPart with FileNamePart {
    private var fileName: String = _

    override def current(previous: String, timeStamp: Long): String = {
      val c = parts.map(_.current(timeStamp)).mkString
      s"$previous/$c"
    }

    override def all(previous: String): Iterator[String] = {
      val regex = parts.map(_.regex).mkString

      val list = Files.list(Paths.get(previous)).iterator()
      val iterator = new Iterator[String] {
        override def hasNext: Boolean = list.hasNext

        override def next(): String = list.next().toString
      }
      iterator.filter { path =>
        val index = path.lastIndexOf('/')
        val fileName = if (index > 0) {
          path.substring(index + 1)
        } else {
          path
        }
        fileName.matches(regex)
      }
    }

    override def before(writer: FileWriter): Unit = {
      val timeStamp = Time()
      val updated = parts.map(_.current(timeStamp)).mkString
      val changed = updated != fileName
      fileName = updated
      if (changed) {
        writer.updatePath()
      }
      parts.foreach(_.before(writer))
    }


    override def after(writer: FileWriter): Unit = {
      parts.foreach(_.after(writer))
    }

    override def current(timeStamp: Long): String = parts.map(_.current(timeStamp)).mkString

    override def regex: String = parts.map(_.regex).mkString

    def %(part: FileNamePart): FileName = copy(parts ::: List(part))
    def %(s: String): FileName = %(string2FileNamePart(s))
  }
}



