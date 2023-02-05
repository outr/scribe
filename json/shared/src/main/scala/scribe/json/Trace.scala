package scribe.json

import fabric.rw._

case class Trace(message: String, elements: List[TraceElement], cause: Option[Trace])

object Trace {
  implicit val rw: RW[Trace] = RW.gen
}