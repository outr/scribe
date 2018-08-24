package scribe.writer.action

import scribe.writer.file.LogFile

case class PathChangedConditionalAction(actions: List[Action]) extends ConditionalAction {
  override def condition(previous: LogFile, current: LogFile): Boolean = {
    previous.differentPath(current)
  }
}
