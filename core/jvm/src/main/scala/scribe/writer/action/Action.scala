package scribe.writer.action

import java.nio.file.Path

import scribe.writer.FileWriter
import scribe.writer.file.{LogFile, LogFileMode}

// TODO: class FileWriter(actions: List[Action], allowNone: Boolean = false)
// TODO: DSL actions applied immediately or added to `actions`

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

case class UpdatePathAction(path: Long => Path, checkRate: Long) extends UpdateLogFileAction {
  private var lastCheck: Long = 0L

  override def update(current: LogFile): LogFile = {
    val now = System.currentTimeMillis()
    try {
      if (now - lastCheck >= checkRate) {
        val newPath = path(now)
        if (FileWriter.isSamePath(Some(current.path), newPath)) {
          current
        } else {
          current.replace(path = newPath)
        }
      } else {
        current
      }
    } finally {
      lastCheck = now
    }
  }
}