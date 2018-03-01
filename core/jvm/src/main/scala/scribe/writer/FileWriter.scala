package scribe.writer

import java.nio.charset.Charset
import java.nio.file.{Path, Paths}

trait FileWriter extends Writer {
  def flush(): Unit
}

object FileWriter {
  object generator {
    def single(prefix: String = "app", suffix: String = ".log"): () => String = () => s"$prefix$suffix"

    def daily(prefix: String = "app", suffix: String = ".log"): () => String = () => {
      val l = System.currentTimeMillis()
      f"$prefix.$l%tY-$l%tm-$l%td$suffix"
    }
  }

  def single(prefix: String = "app",
             suffix: String = ".log",
             directory: Path = Paths.get("logs"),
             append: Boolean = true,
             autoFlush: Boolean = true,
             charset: Charset = Charset.defaultCharset(),
             nio: Boolean = false): FileWriter = if (nio) {
    new FileNIOWriter(directory, generator.single(prefix, suffix), append, autoFlush, charset)
  } else {
    new FileIOWriter(directory, generator.single(prefix, suffix), append, autoFlush, charset)
  }

  def daily(prefix: String = "app",
            suffix: String = ".log",
            directory: Path = Paths.get("logs"),
            append: Boolean = true,
            autoFlush: Boolean = true,
            charset: Charset = Charset.defaultCharset(),
            nio: Boolean = false): FileWriter = if (nio) {
    new FileNIOWriter(directory, generator.daily(prefix, suffix), append, autoFlush, charset)
  } else {
    new FileIOWriter(directory, generator.daily(prefix, suffix), append, autoFlush, charset)
  }
}
