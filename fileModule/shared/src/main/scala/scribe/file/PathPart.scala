package scribe.file

import scribe.util.Time

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

import perfolation._

trait PathPart {
  def current(previous: Path): Path

  def all(previous: Path): List[Path]

  def revalidate(): Boolean = false
}

object PathPart {
  case object Root extends PathPart {
    override def current(previous: Path): Path = Paths.get("/")

    override def all(previous: Path): List[Path] = List(current(previous))
  }

  case class SetPath(path: Path) extends PathPart {
    override def current(previous: Path): Path = path

    override def all(previous: Path): List[Path] = List(path)
  }

  case class Static(part: String) extends PathPart {
    override def current(previous: Path): Path = previous.resolve(part)

    override def all(previous: Path): List[Path] = List(current(previous))
  }

  case class FileName(parts: List[FileNamePart]) extends PathPart {
    private var fileName: String = _

    override def current(previous: Path): Path = {

      previous.resolve(parts.map(_.current).mkString)
    }

    override def all(previous: Path): List[Path] = {
      val regex = parts.map(_.regex).mkString
      Files.list(previous).iterator().asScala.toList.filter { path =>
        val fileName = path.getFileName.toString
        fileName.matches(regex)
      }
    }

    override def revalidate(): Boolean = {
      val updated = parts.map(_.current).mkString
      val changed = updated != fileName
      fileName = updated
      changed
    }

    def %(part: FileNamePart): FileName = copy(parts ::: List(part))
  }
}

trait FileNamePart {
  def current: String
  def regex: String
}

object FileNamePart {
  case class Static(s: String) extends FileNamePart {
    override def current: String = s

    override def regex: String = s
  }
  case object Year extends FileNamePart {
    override def current: String = Time().t.year.toString

    override def regex: String = "\\d{4}"
  }
  case object Month extends FileNamePart {
    override def current: String = Time().t.m

    override def regex: String = "\\d{2}"
  }
  case object Day extends FileNamePart {
    override def current: String = Time().t.d

    override def regex: String = "\\d{2}"
  }
}

// directory / prefix % separator % Year % separator % Month % separator % Day % ".log"