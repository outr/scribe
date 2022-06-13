package scribe

import scribe.file.path.PathPart.FileName
import scribe.file.path._

import java.io.File
import java.nio.file.Path
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.implicitConversions

package object file {
  val DefaultBufferSize: Int = 1024

  implicit def pathPart2PathBuilder(part: PathPart): PathBuilder = PathBuilder(List(part))
  implicit def path2PathBuilder(path: Path): PathBuilder = PathBuilder(List(PathPart.SetPath(path.toAbsolutePath.toString)))
  implicit def file2PathBuilder(file: File): PathBuilder = PathBuilder(List(PathPart.SetPath(file.getAbsolutePath)))
  implicit def string2PathBuilder(s: String): PathBuilder = PathBuilder(List(PathPart.SetPath(s)))
  implicit def string2FileName(s: String): FileName = FileName(List(FileNamePart.Static(s)))
  implicit def string2FileNamePart(s: String): FileNamePart = FileNamePart.Static(s)
  implicit def fileNamePart2FileName(part: FileNamePart): FileName = FileName(List(part))

  def second: FileNamePart = FileNamePart.Second
  def minute: FileNamePart = FileNamePart.Minute
  def hour: FileNamePart = FileNamePart.Hour
  def day: FileNamePart = FileNamePart.Day
  def month: FileNamePart = FileNamePart.Month
  def year: FileNamePart = FileNamePart.Year
  def rolling(fileName: FileName,
              truncate: Boolean = true,
              minimumValidationFrequency: FiniteDuration = 5.minutes): FileNamePart = {
    Rolling(fileName.parts, (current, path) => {
      LogFile.get(current) match {
        case Some(logFile) => {
          if (truncate) {
            LogFile.copy(logFile, path)
            LogFile.truncate(logFile)
          } else {
            LogFile.move(logFile, path)
          }
        }
        case None => {
          if (truncate) {
            LogFile.copy(current, path)
            LogFile.truncate(current)
          } else {
            LogFile.move(current, path)
          }
        }
      }
    }, minimumValidationFrequency)
  }
  def rollingGZIP(fileName: FileName = string2FileName(".gz"),
                  deleteOriginal: Boolean = true,
                  bufferSize: Int = DefaultBufferSize,
                  minimumValidationFrequency: FiniteDuration = 5.minutes): FileNamePart = {
    Rolling(fileName.parts, (current, path) => {
      LogFile.get(current) match {
        case Some(logFile) => LogFile.gzip(logFile, path, deleteOriginal, bufferSize)
        case None => LogFile.gzip(current, path, deleteOriginal, bufferSize)
      }
    }, minimumValidationFrequency)
  }
  def maxSize(max: Long = MaxSize.OneHundredMeg, separator: String = "-"): FileNamePart = MaxSize(max, separator)
  def maxLogs(max: Int = 10, checkFrequency: FiniteDuration = 15.minutes): FileNamePart = MaxLogs(max, checkFrequency)

  def daily(separator: String = "-"): FileName = year % separator % month % separator % day
}