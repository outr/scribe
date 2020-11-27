package scribe.writer.file

import java.io.{File, FileWriter, PrintWriter}

class IOLogFileWriter(lf: LogFile) extends LogFileWriter {
  private lazy val file: File = lf.path.toAbsolutePath.toFile
  private lazy val writer: PrintWriter = new PrintWriter(new FileWriter(file, lf.append))

  override def write(output: String): Unit = Option(output) match {
    case Some(o) => writer.write(o)
    case None => writer.write("null")
  }

  override def flush(): Unit = writer.flush()

  override def dispose(): Unit = writer.close()
}
