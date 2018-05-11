package scribe.writer

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}

import scribe.writer.manager._

trait FileWriter extends Writer {
  def flush(): Unit
}

object FileWriter {
  def single(prefix: String = "app",
             suffix: String = ".log",
             directory: Path = Paths.get("logs"),
             append: Boolean = true,
             autoFlush: Boolean = false,
             charset: Charset = Charset.defaultCharset(),
             nio: Boolean = false): FileWriter = {
    val manager = FlatFileLoggingManager(directory.resolve(s"$prefix$suffix"))
    apply(manager, append, autoFlush, charset, nio)
  }

  def daily(prefix: String = "app",
            suffix: String = ".log",
            directory: Path = Paths.get("logs"),
            append: Boolean = true,
            autoFlush: Boolean = false,
            charset: Charset = Charset.defaultCharset(),
            nio: Boolean = false): FileWriter = {
    val fileName = (l: Long) => {
      f"$prefix.$l%tY-$l%tm-$l%td$suffix"
    }
    val manager = FormattedFileLoggingManager(directory, fileName)
    apply(manager, append, autoFlush, charset, nio)
  }

  def rolling(prefix: String = "app",
              suffix: String = ".log",
              directory: Path = Paths.get("logs"),
              append: Boolean = true,
              autoFlush: Boolean = false,
              charset: Charset = Charset.defaultCharset(),
              nio: Boolean = false): FileWriter = {
    val archive = (l: Long) => {
      f"$prefix.$l%tY-$l%tm-$l%td$suffix"
    }
    val manager = RollingFormattedFileLoggingManager(directory, s"$prefix$suffix", archive)
    apply(manager, append, autoFlush, charset, nio)
  }

  def apply(manager: FileLoggingManager,
            append: Boolean = true,
            autoFlush: Boolean = false,
            charset: Charset = Charset.defaultCharset(),
            nio: Boolean = false): FileWriter = if (nio) {
    FileNIOWriter(manager, append, autoFlush, charset)
  } else {
    FileIOWriter(manager, append, autoFlush, charset)
  }

  protected[writer] def validate(current: Option[Path], resolution: PathResolution): Option[Path] = {
    current match {
      case Some(existing) => {
        val newPath = resolution.path
        val existingExists = Files.exists(existing)
        val newExists = Files.exists(newPath)
        val sameFile = if (existingExists) {
          if (newExists) {
            Files.isSameFile(existing, newPath)
          } else {
            false
          }
        } else {
          existing.toAbsolutePath.toString == newPath.toAbsolutePath.toString
        }
        if (sameFile) {
          None
        } else {
          Some(newPath)
        }
      }
      case None => Some(resolution.path)
    }
  }
}