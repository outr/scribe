package scribe.writer

import java.nio.file.{Files, Path}

import scribe._
import scribe.writer.file.LogFile
import scribe.writer.file.action.Action

class FileWriter(actions: List[Action], allowNone: Boolean = false) extends Writer {
  @volatile private[writer] var logFile: Option[LogFile] = None

  protected def validate(): Unit = {
    val updated = Action(actions, logFile, None)
    if (updated != logFile) {
      logFile.foreach(_.dispose())
      logFile = updated
      if (!allowNone && logFile.isEmpty) throw new RuntimeException("FileWriter actions resulted in no LogFile being returned!")
    }
  }

  override def write[M](record: LogRecord[M], output: String): Unit = synchronized {
    validate()
    logFile.foreach(_.write(output))
  }

  def flush(): Unit = logFile.foreach(_.flush())

  override def dispose(): Unit = {
    super.dispose()

    logFile.foreach(_.dispose())
  }
}

object FileWriter extends FileWriter(Nil, allowNone = false) {
  def isSamePath(oldPath: Option[Path], newPath: Path): Boolean = oldPath match {
    case Some(current) => if (current == newPath) {
      true
    } else if (Files.exists(current)) {
      if (Files.exists(newPath)) {
        Files.isSameFile(current, newPath)
      } else {
        false
      }
    } else {
      current.toAbsolutePath.toString == newPath.toAbsolutePath.toString
    }
    case None => false
  }
}