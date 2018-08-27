package scribe.writer.action

import scribe.writer.file.LogFile

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
