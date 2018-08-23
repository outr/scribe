package scribe.writer.action

import java.nio.file.{Files, Path}

import scribe.util.Time
import scribe.writer.FileWriter
import scribe.writer.file.{LogFile, LogFileMode}
import scala.collection.JavaConverters._

trait Action {
  def apply(previous: LogFile, current: LogFile): LogFile

  @volatile private var lastCall: Long = 0L
  protected def rateDelayed(rate: Long, current: LogFile)(f: => LogFile): LogFile = {
    val now = Time()
    if (now - lastCall >= rate) {
      lastCall = now
      f
    } else {
      current
    }
  }
}

object Action {
  def apply(actions: List[Action], previous: LogFile, current: LogFile): LogFile = {
    if (actions.isEmpty) {
      current
    } else {
      val action = actions.head
      val updated = action(previous, current)
      apply(actions.tail, previous, updated)
    }
  }
}

trait ConditionalAction extends Action {
  def condition(previous: LogFile, current: LogFile): Boolean
  def actions: List[Action]

  override def apply(previous: LogFile, current: LogFile): LogFile = {
    if (condition(previous, current)) {
      Action(actions, previous, current)
    } else {
      current
    }
  }
}

case class PathChangedConditionalAction(actions: List[Action]) extends ConditionalAction {
  override def condition(previous: LogFile, current: LogFile): Boolean = {
    previous.differentPath(current)
  }
}

trait UpdateLogFileAction extends Action {
  def update(current: LogFile): LogFile

  override def apply(previous: LogFile, current: LogFile): LogFile = {
    update(current)
  }
}

object UpdateLogFileAction {
  def apply(f: LogFile => LogFile): Action = new UpdateLogFileAction {
    override def update(current: LogFile): LogFile = f(current)
  }
}

case class UpdatePathAction(path: Long => Path, gzip: Boolean, checkRate: Long) extends UpdateLogFileAction {
  override def update(current: LogFile): LogFile = rateDelayed(checkRate, current) {
    val newPath = path(Time())
    if (FileWriter.samePath(current.path, newPath)) {
      current
    } else {
      val replacement = current.replace(path = newPath)
      if (gzip) {
        current.gzip()
      }
      replacement
    }
  }
}

case class PathResolvingAction(path: Long => Path, gzip: Boolean, checkRate: Long) extends Action {
  @volatile private var currentFileStamp: Long = 0L

  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    if ((current != previous && previous.differentPath(current)) || currentFileStamp == 0L) {
      if (Files.exists(current.path)) {
        currentFileStamp = Files.getLastModifiedTime(current.path).toMillis
      }
      current
    } else {
      val previousPath = path(currentFileStamp)
      val currentPath = path(Time())
      if (FileWriter.differentPath(previousPath, currentPath)) {
        val renamed = current.rename(currentPath)
        if (gzip) {
          renamed.gzip()
        }
        current.replace()
      } else {
        current
      }
    }
  }
}

case class MaxLogFilesAction(max: Int,
                             lister: Path => List[Path],
                             checkRate: Long) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    val logs = lister(current.path)
    if (logs.length > max) {
      logs.take(logs.length - max).foreach { path =>
        Files.deleteIfExists(path)
      }
    }
    current
  }
}

object MaxLogFilesAction {
  val MatchLogAndGZ: Path => Boolean = (path: Path) => {
    val name = path.toString.toLowerCase
    name.endsWith(".log") || name.endsWith(".log.gz")
  }

  val MatchLogAndGZInSameDirectory: Path => List[Path] = (path: Path) => {
    val directory = Option(path.toAbsolutePath.getParent)
      .getOrElse(throw new RuntimeException(s"No parent found for ${path.toAbsolutePath.toString}"))
    Files.list(directory).iterator().asScala.filter(MatchLogAndGZ).toList.sortBy(Files.getLastModifiedTime(_)).reverse
  }
}

case class MaxLogSizeAction(maxSizeInBytes: Long,
                            actions: List[Action],
                            checkRate: Long) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = rateDelayed(checkRate, current) {
    if (current.size >= maxSizeInBytes) {
      Action(actions, previous, current)
    } else {
      current
    }
  }
}