package scribe.file.path

import scribe.file.{FileWriter, LogFile}

import java.nio.file.{Files, Path}

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

  // TODO: Optimize this by determining next time it should check
  override def before(writer: FileWriter): Unit = {
    val currentPaths: List[Path] = {
      threadLocal.set(Rolling.OnlyCurrent)
      try {
        writer.list()
      } finally {
        threadLocal.remove()
      }
    }
    val existing: Path = {
      threadLocal.set(Rolling.OnlyRolling)
      try {
        writer.resolvePath()
      } finally {
        threadLocal.remove()
      }
    }

    currentPaths.foreach { cp =>
      val lastModified = Files.getLastModifiedTime(cp).toMillis
      val rp = rollingPath(lastModified, writer)
      LogFile.get(cp) match {
        case Some(logFile) if rp != existing && !Files.exists(rp) => action(logFile, rp)
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