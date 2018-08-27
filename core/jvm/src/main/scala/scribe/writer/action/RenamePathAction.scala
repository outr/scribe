package scribe.writer.action

import java.nio.file.Path

import scribe.writer.file.LogFile

case class RenamePathAction(renamer: Path => Path, useRenamed: Boolean) extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = {
    val renamed = current.rename(renamer(current.path))
    if (useRenamed) {
      renamed
    } else {
      current.replace()
    }
  }
}
