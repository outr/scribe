package scribe.file.path

import scribe.util.Time

import java.nio.file.{Files, Path, Paths}
import scribe.file.{FileWriter, string2FileNamePart}

trait PathPart {
  def current(previous: Path, timeStamp: Long): Path

  def all(previous: Path): List[Path]

  def before(writer: FileWriter): Unit = {}
  def after(writer: FileWriter): Unit = {}
}

object PathPart {
  case object Root extends PathPart {
    override def current(previous: Path, timeStamp: Long): Path = Paths.get("/")

    override def all(previous: Path): List[Path] = List(current(previous, 0L))
  }

  case class SetPath(path: Path) extends PathPart {
    override def current(previous: Path, timeStamp: Long): Path = path

    override def all(previous: Path): List[Path] = List(path)
  }

  case class Static(part: String) extends PathPart {
    override def current(previous: Path, timeStamp: Long): Path = previous.resolve(part)

    override def all(previous: Path): List[Path] = List(current(previous, 0L))
  }

  case class FileName(parts: List[FileNamePart]) extends PathPart with FileNamePart {
    private var fileName: String = _

    override def current(previous: Path, timeStamp: Long): Path = {
      previous.resolve(parts.map(_.current(timeStamp)).mkString)
    }

    override def all(previous: Path): List[Path] = {
      val regex = parts.map(_.regex).mkString
      Files.list(previous).toArray.toList.asInstanceOf[List[Path]].filter { path =>
        val fileName = path.getFileName.toString
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



