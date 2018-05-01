package scribe.writer.manager

import java.nio.file.{Files, Path}

case class RollingFormattedFileLoggingManager(directory: Path, current: String, archive: Long => String) extends FileLoggingManager {
  private val path: Path = directory.resolve(current)

  override def derivePath: PathResolution = {
    val changed = if (Files.exists(path)) {
      val lastModified = Files.getLastModifiedTime(path).toMillis
      val currentFile = archive(lastModified)
      val newFile = archive(System.currentTimeMillis())
      if (currentFile != newFile) {
        Some(new ChangeHandler {
          override def change(): Unit = {
            Files.move(path, directory.resolve(currentFile))
          }
        })
      } else {
        None
      }
    } else {
      None
    }
    PathResolution(path, changed)
  }
}
