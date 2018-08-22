package scribe.writer.file.action

import scribe.writer.file.LogFile

// TODO: class FileWriter(actions: List[Action], allowNone: Boolean = false)
// TODO: DSL actions applied immediately or added to `actions`

trait Action {
  def apply(previous: Option[LogFile], current: Option[LogFile]): Option[LogFile]
}

object Action {
  def apply(actions: List[Action], previous: Option[LogFile], current: Option[LogFile]): Option[LogFile] = {
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
  def condition(previous: Option[LogFile], current: Option[LogFile]): Boolean
  def actions: List[Action]

  override def apply(previous: Option[LogFile], current: Option[LogFile]): Option[LogFile] = {
    if (condition(previous, current)) {
      Action(actions, previous, current)
    } else {
      current
    }
  }
}

trait UpdateLogFileAction extends Action {
  def create(): Option[LogFile] = None
  def update(current: LogFile): LogFile

  override def apply(previous: Option[LogFile], current: Option[LogFile]): Option[LogFile] = {
    current.orElse(previous).orElse(create()).map { logFile =>
      update(logFile)
    }
  }
}