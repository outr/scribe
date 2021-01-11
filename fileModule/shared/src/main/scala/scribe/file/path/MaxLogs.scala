package scribe.file.path

import scribe.file.FileWriter

import java.nio.file.Files

case class MaxLogs(maxLogs: Int) extends FileNamePart {
  override def current(timeStamp: Long): String = ""

  override def regex: String = ""

  override def after(writer: FileWriter): Unit = {
    writer.list().dropRight(maxLogs).foreach { path =>
      Files.delete(path)
    }
  }
}