package scribe.file.writer

import scribe.file.LogFile

import java.io.{File, FileWriter, PrintWriter}

class IOLogFileWriter(lf: LogFile) extends LogFileWriter {
  private lazy val file: File = lf.path.toAbsolutePath.toFile
  private lazy val writer: PrintWriter = new PrintWriter(new FileWriter(file, lf.append))

  override def write(output: String): Unit = if (output == None.orNull) {
    writer.write("null")
  } else {
    writer.write(output)
  }

  override def flush(): Unit = writer.flush()

  override def dispose(): Unit = writer.close()
}