package scribe.writer

import java.nio.file.{Files, Path}

import scribe._
import scribe.writer.file.{LogFile, LogFileMode, LogPath}
import scribe.writer.action._

import scala.concurrent.duration._

class FileWriter(actions: List[Action]) extends Writer {
  @volatile private[writer] var logFile: LogFile = LogFile(LogPath.default(0L))

  def invoke(actions: List[Action]): FileWriter = synchronized {
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

  def withMode(mode: LogFileMode): FileWriter = invoke(List(UpdateLogFileAction(_.replace(mode = mode))))

  def nio: FileWriter = withMode(LogFileMode.NIO)

  def io: FileWriter = withMode(LogFileMode.IO)

  def append: FileWriter = invoke(List(UpdateLogFileAction(_.replace(append = true))))

  def replace: FileWriter = invoke(List(UpdateLogFileAction(_.replace(append = false))))

  def autoFlush: FileWriter = invoke(List(UpdateLogFileAction(_.replace(autoFlush = true))))

  def withActions(actions: Action*): FileWriter = {
    dispose()
    new FileWriter(this.actions ::: actions.toList)
  }

  def path(path: Long => Path, gzip: Boolean = false, checkRate: FiniteDuration = FileWriter.DefaultCheckRate): FileWriter = {
    withActions(UpdatePathAction(path, gzip, checkRate))
  }

  def rolling(path: Long => Path, gzip: Boolean = false, checkRate: FiniteDuration = FileWriter.DefaultCheckRate): FileWriter = {
    withActions(PathResolvingAction(path, gzip, checkRate))
  }

  def maxLogs(max: Int,
              lister: Path => List[Path] = MaxLogFilesAction.MatchLogAndGZInSameDirectory,
              logManager: Path => Unit = Files.deleteIfExists(_),
              checkRate: FiniteDuration = FileWriter.DefaultCheckRate): FileWriter = {
    withActions(MaxLogFilesAction(max, lister, logManager, checkRate))
  }

  def maxSize(maxSizeInBytes: Long,
              actions: List[Action] = List(DeletePathAction),
              checkRate: FiniteDuration = FileWriter.DefaultCheckRate): FileWriter = {
    withActions(MaxLogSizeAction(maxSizeInBytes, actions, checkRate))
  }

  def flush(): Unit = logFile.flush()

  override def dispose(): Unit = {
    super.dispose()

    logFile.dispose()
  }
}

object FileWriter {
  val DefaultCheckRate: FiniteDuration = 100.millis

  def apply(): FileWriter = new FileWriter(Nil)

  def differentPath(p1: Path, p2: Path): Boolean = {
    if (p1 == p2) {
      false
    } else if (Files.exists(p1)) {
      if (Files.exists(p2)) {
        !Files.isSameFile(p1, p2)
      } else {
        true
      }
    } else {
      p1.toAbsolutePath.toString != p2.toAbsolutePath.toString
    }
  }

  def samePath(p1: Path, p2: Path): Boolean = !differentPath(p1, p2)
}