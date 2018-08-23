package scribe.writer.action

import java.nio.file.{Files, Path}

import scribe.util.Time
import scribe.writer.FileWriter
import scribe.writer.file.{LogFile, LogFileMode}

trait Action {
  def apply(previous: LogFile, current: LogFile): LogFile
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
    !FileWriter.isSamePath(Some(previous.path), current.path)
  }
}

trait UpdateLogFileAction extends Action {
  def update(current: LogFile): LogFile

  override def apply(previous: LogFile, current: LogFile): LogFile = {
    update(current)
  }
}

case class FileModeAction(mode: LogFileMode) extends UpdateLogFileAction {
  override def update(current: LogFile): LogFile = if (current.mode != mode) {
    current.replace(mode = mode)
  } else {
    current
  }
}

case class UpdatePathAction(path: Long => Path, gzip: Boolean, checkRate: Long) extends UpdateLogFileAction {
  private var lastCheck: Long = 0L

  override def update(current: LogFile): LogFile = {
    val now = Time()
    try {
      if (now - lastCheck >= checkRate) {
        val newPath = path(now)
        if (FileWriter.isSamePath(Some(current.path), newPath)) {
          current
        } else {
          val replacement = current.replace(path = newPath)
          if (gzip) {
            current.gzip()
          }
          replacement
        }
      } else {
        current
      }
    } finally {
      lastCheck = now
    }
  }
}

case class RenamePathAction(path: Long => Path, gzip: Boolean, checkRate: Long) extends Action {
  private var lastCheck: Long = 0L
  @volatile private var currentFileStamp: Long = 0L

  override def apply(previous: LogFile, current: LogFile): LogFile = {
    val now = Time()
    try {
      if (now - lastCheck >= checkRate) {
        if ((current != previous && !FileWriter.isSamePath(Some(previous.path), current.path)) || currentFileStamp == 0L) {
          if (Files.exists(current.path)) {
            currentFileStamp = Files.getLastModifiedTime(current.path).toMillis
          }
          current
        } else {
          val previousPath = path(currentFileStamp)
          val currentPath = path(Time())
          if (!FileWriter.isSamePath(Some(previousPath), currentPath)) {
            val renamed = current.rename(currentPath)
            if (gzip) {
              renamed.gzip()
            }
            current.replace()
          } else {
            current
          }
        }
      } else {
        current
      }
    } finally {
      lastCheck = now
    }
  }
}