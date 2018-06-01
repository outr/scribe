package scribe.writer.file

trait LogFileMode {
  def key: String

  def createWriter(lf: LogFile): LogFileWriter
}

object LogFileMode {
  case object IO extends LogFileMode {
    override def key: String = "io"

    override def createWriter(lf: LogFile): LogFileWriter = new IOLogFileWriter(lf)
  }

  case object NIO extends LogFileMode {
    override def key: String = "nio"

    override def createWriter(lf: LogFile): LogFileWriter = new NIOLogFileWriter(lf)
  }
}