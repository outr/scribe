package scribe.writer

sealed trait ContentSupport

object ContentSupport {
  case object PlainText extends ContentSupport
  case object Rich extends ContentSupport
}