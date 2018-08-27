package scribe.writer.action

import scribe.writer.file.LogFile

object DeletePathAction extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = {
    current.delete()
    current.replace()
  }
}
