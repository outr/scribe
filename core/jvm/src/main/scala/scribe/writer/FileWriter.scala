package scribe.writer

import java.nio.file.{Files, Path, Paths}

import scribe._
import scribe.writer.file.{LogFile, LogFileMode, LogPath}
import scribe.writer.action.{Action, FileModeAction, RenamePathAction, UpdatePathAction}

class FileWriter(actions: List[Action]) extends Writer {
  @volatile private[writer] var logFile: LogFile = LogFile(LogPath.default(0L))

  def invoke(actions: List[Action]): FileWriter = {
    val updated = Action(actions, logFile, logFile)
    if (updated != logFile) {
      if (logFile.isActive) {
        logFile.dispose()
      }
      logFile = updated
    }
    this
  }

  override def write[M](record: LogRecord[M], output: String): Unit = synchronized {
    invoke(actions)
    logFile.write(output)
  }

  def nio: FileWriter = invoke(List(FileModeAction(LogFileMode.NIO)))

  def io: FileWriter = invoke(List(FileModeAction(LogFileMode.IO)))

  def path(path: Long => Path, gzip: Boolean = false, checkRate: Long = 10L): FileWriter = {
    invoke(List(UpdatePathAction(path, gzip, checkRate)))
  }

  def withActions(actions: Action*): FileWriter = {
    dispose()
    new FileWriter(this.actions ::: actions.toList)
  }

  def rolling(path: Long => Path, gzip: Boolean = false, checkRate: Long = 10L): FileWriter = {
    withActions(RenamePathAction(path, gzip, checkRate))
  }

  def flush(): Unit = logFile.flush()

  override def dispose(): Unit = {
    super.dispose()

    logFile.dispose()
  }
}

object FileWriter {
  def apply(): FileWriter = new FileWriter(Nil)

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