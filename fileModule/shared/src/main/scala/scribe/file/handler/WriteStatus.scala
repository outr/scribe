package scribe.file.handler

sealed trait WriteStatus

object WriteStatus {
  case object Before extends WriteStatus
  case object After extends WriteStatus
}