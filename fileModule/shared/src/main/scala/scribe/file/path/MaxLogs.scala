package scribe.file.path

import scribe.file.FileWriter
import scribe.util.Time

import java.nio.file.Files
import scala.concurrent.duration._

case class MaxLogs(maxLogs: Int, checkFrequency: FiniteDuration) extends FileNamePart {
  private var nextRun: Long = 0L

  override def current(timeStamp: Long): String = ""

  override def regex: String = ""

  override def after(writer: FileWriter): Unit = if (Time() >= nextRun) {
    writer.list().dropRight(maxLogs).foreach { file =>
      if (!file.delete()) {
        file.deleteOnExit()
      }
    }
    nextRun = Time() + checkFrequency.toMillis
  }
}