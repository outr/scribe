package scribe.writer

import java.nio.file.{Files, Path, Paths}

import scribe._
import scribe.writer.file.{LogFile, LogFileMode}
import scribe.writer.action.{Action, FileModeAction, UpdatePathAction}

class FileWriter(actions: List[Action]) extends Writer {
  @volatile private[writer] var logFile: LogFile = LogFile(FileWriter.DefaultPath)

  protected def validate(actions: List[Action]): FileWriter = {
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
    validate(actions)
    logFile.write(output)
  }

  def nio: FileWriter = validate(List(FileModeAction(LogFileMode.NIO)))

  def io: FileWriter = validate(List(FileModeAction(LogFileMode.IO)))

  def path(path: => Path, checkRate: Long = 10L): FileWriter = validate(List(UpdatePathAction(_ => path, checkRate)))
  def path(path: Long => Path, checkRate: Long = 10L): FileWriter = validate(List(UpdatePathAction(path, checkRate)))

  def withActions(actions: Action*): FileWriter = {
    dispose()
    new FileWriter(this.actions ::: actions.toList)
  }

  def rolling() // TODO: implement

  def flush(): Unit = logFile.flush()

  override def dispose(): Unit = {
    super.dispose()

    logFile.dispose()
  }
}

object FileWriter {
  lazy val DefaultPath: Path = Paths.get("app.log")

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