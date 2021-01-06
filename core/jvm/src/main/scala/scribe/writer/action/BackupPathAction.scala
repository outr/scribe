package scribe.writer.action

import java.nio.file.{Files, Path, Paths}

import scribe.writer.file.LogFile

object BackupPathAction extends Action {
  override def apply(previous: LogFile, current: LogFile): LogFile = {
    current.dispose()
    pushBackups(current.path)
    Files.createFile(current.path)
    current.replace()
  }

  private def pushBackups(path: Path, increment: Int = 1): Unit = {
    val backup = backupPath(path, increment)
    if (Files.exists(backup)) {
      pushBackups(path, increment + 1)
    }
    val current = backupPath(path, increment - 1)
    val lastModified = Files.getLastModifiedTime(current)
    Files.move(current, backup)
    Files.setLastModifiedTime(backup, lastModified)
  }

  private def backupPath(path: Path, increment: Int): Path = if (increment > 0) {
    val absolute = path.toAbsolutePath.toString
    val idx = absolute.lastIndexOf('.')
    val absolutePath = s"${absolute.substring(0, idx)}.$increment.${absolute.substring(idx + 1)}"
    Paths.get(absolutePath)
  } else {
    path
  }
}