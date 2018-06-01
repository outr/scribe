package scribe.writer

import java.nio.file.{Files, Path}

import scribe.LogRecord
import scribe.writer.file.LogFile

import scala.annotation.tailrec

class MaxSizePathBuilder(maximumSizeInBytes: Long,
                         renamer: Path => Path,
                         chain: PathBuilder) extends PathBuilder {
  override def derivePath[M](writer: FileWriter, record: LogRecord[M]): Option[LogFile] = {
    val result = chain.derivePath(writer, record)
    writer.logFile match {
      case Some(lf) if result.isEmpty && lf.size >= maximumSizeInBytes => {
        lf.rename(renamer(lf.path))
        Some(LogFile(lf.path, lf.append, lf.autoFlush, lf.charset, lf.mode))
      }
      case _ => result
    }
  }
}

object MaxSizePathBuilder {
  @tailrec
  final def findNext(directory: Path, generator: Int => String, increment: Int = 1): Path = {
    val fileName = generator(increment)
    val path = directory.resolve(fileName)
    if (!Files.exists(path)) {
      path
    } else {
      findNext(directory, generator, increment + 1)
    }
  }
}