package scribe.file

sealed trait LogFileStatus

object LogFileStatus {
  case object Inactive extends LogFileStatus
  case object Active extends LogFileStatus
  case object Disposed extends LogFileStatus
}