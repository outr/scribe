package scribe.writer

import java.nio.charset.Charset
import java.nio.file.{Path, Paths}

import scribe.writer.manager.{FileLoggingManager, FlatFileLoggingManager, FormattedFileLoggingManager, RollingFormattedFileLoggingManager}

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
}