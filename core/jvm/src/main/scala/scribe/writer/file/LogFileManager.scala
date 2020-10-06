package scribe.writer.file

import java.nio.file.{Files, Path}
import perfolation._

trait LogFileManager {
  def replace(oldLogFile: Option[LogFile], newLogFile: LogFile): Unit
}

object LogFileManager {
  case class MaximumLogs(max: Int, list: Path => List[Path]) extends LogFileManager {
    override def replace(oldLogFile: Option[LogFile], newLogFile: LogFile): Unit = {
      val logs = list(newLogFile.path)
      if (logs.length > max) {
        logs.take(logs.length - max).foreach { path =>
          Files.deleteIfExists(path)
        }
      }
    }
  }

  case class Rename(renamer: Path => Path) extends LogFileManager {
    override def replace(oldLogFile: Option[LogFile], newLogFile: LogFile): Unit = {
      oldLogFile.foreach { logFile =>
        val newPath = renamer(logFile.path)
        logFile.rename(newPath)
      }
    }
  }

  case class GZip(fileName: String => String = (fn: String) => s"$fn.gz",
                  deleteOriginal: Boolean = true) extends LogFileManager {
    override def replace(oldLogFile: Option[LogFile], newLogFile: LogFile): Unit = {
      oldLogFile.foreach { logFile =>
        logFile.gzip(fileName(logFile.path.getFileName.toString), deleteOriginal)
      }
    }
  }

  case object Dispose extends LogFileManager {
    override def replace(oldLogFile: Option[LogFile], newLogFile: LogFile): Unit = {
      oldLogFile.foreach(_.dispose())
    }
  }

  case object Delete extends LogFileManager {
    override def replace(oldLogFile: Option[LogFile], newLogFile: LogFile): Unit = {
      oldLogFile.foreach(_.delete())
    }
  }

  case class Grouped(managers: List[LogFileManager]) extends LogFileManager {
    override def replace(oldLogFile: Option[LogFile], newLogFile: LogFile): Unit = managers.foreach { m =>
      m.replace(oldLogFile, newLogFile)
    }
  }
}