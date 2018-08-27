package scribe.writer.action

import scribe.writer.file.LogFile

case class ActionContainer(actions: List[Action]) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = Action(actions, previous, current)
}
