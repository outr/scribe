package scribe.writer

import java.nio.file.Path

package object action {
  def rename(renamer: Path => Path, useRenamed: Boolean): Action = RenamePathAction(renamer, useRenamed)
  def backup: Action = BackupPathAction
  def delete: Action = DeletePathAction
  def actions(actions: Action*): Action = ActionContainer(actions.toList)
}