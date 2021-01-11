package scribe.file

import scribe.util.Time

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

import perfolation._

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

  case class FileName(parts: List[FileNamePart]) extends PathPart {
    private var fileName: String = _

    override def current(previous: Path, timeStamp: Long): Path = {
      previous.resolve(parts.map(_.current(timeStamp)).mkString)
    }

    override def all(previous: Path): List[Path] = {
      val regex = parts.map(_.regex).mkString
      Files.list(previous).iterator().asScala.toList.filter { path =>
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

    def %(part: FileNamePart): FileName = copy(parts ::: List(part))
  }
}

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
  case class Rolling(parts: List[FileNamePart], action: (LogFile, Path) => Unit) extends FileNamePart {
    private lazy val partsRegex = parts.map(_.regex).mkString
    private val threadLocal = new ThreadLocal[Rolling.Mode] {
      override def initialValue(): Rolling.Mode = Rolling.Standard
    }

    override def current(timeStamp: Long): String = threadLocal.get() match {
      case Rolling.Standard | Rolling.OnlyCurrent => ""
      case Rolling.OnlyRolling => parts.map(_.current(timeStamp)).mkString
    }

    override def regex: String = threadLocal.get() match {
      case Rolling.Standard => s"($partsRegex)?"
      case Rolling.OnlyCurrent => ""
      case Rolling.OnlyRolling => partsRegex
    }

    override def before(writer: FileWriter): Unit = {
      val currentPaths: List[Path] = {
        threadLocal.set(Rolling.OnlyCurrent)
        try {
          writer.list()
        } finally {
          threadLocal.remove()
        }
      }

      currentPaths.foreach { cp =>
        val lastModified = Files.getLastModifiedTime(cp).toMillis
        val rp = rollingPath(lastModified, writer)
        LogFile.get(cp) match {
          case Some(logFile) if !Files.exists(rp) => action(logFile, rp)
          case _ => // Ignore
        }
      }
    }

    def rollingPath(timeStamp: Long, writer: FileWriter): Path = {
      threadLocal.set(Rolling.OnlyRolling)
      try {
        writer.pathBuilder.path(timeStamp)
      } finally {
        threadLocal.remove()
      }
    }
  }

  object Rolling {
    sealed trait Mode

    case object Standard extends Mode
    case object OnlyCurrent extends Mode
    case object OnlyRolling extends Mode
  }
}