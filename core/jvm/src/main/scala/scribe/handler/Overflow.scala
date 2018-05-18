package scribe.handler

sealed trait Overflow

object Overflow {
  case object DropOld extends Overflow
  case object DropNew extends Overflow
  case object Block extends Overflow
  case object Error extends Overflow
}