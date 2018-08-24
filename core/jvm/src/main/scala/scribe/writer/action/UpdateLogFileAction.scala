package scribe.writer.action

import scribe.writer.file.LogFile

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