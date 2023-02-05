package scribe.json

import fabric.rw._

case class TraceElement(`class`: String, method: String, line: Int)

object TraceElement {
  implicit val rw: RW[TraceElement] = RW.gen
}