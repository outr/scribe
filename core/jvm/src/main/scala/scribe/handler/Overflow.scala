package scribe.handler

sealed trait Overflow

/**
  * Overflow instructions for AsynchronousLogHandler
  */
object Overflow {
  /**
    * Drops oldest over max buffer
    */
  case object DropOld extends Overflow

  /**
    * Drops the new messages
    */
  case object DropNew extends Overflow

  /**
    * Blocks until the buffer falls below max
    */
  case object Block extends Overflow

  /**
    * Throws an exception if the buffer overflows
    */
  case object Error extends Overflow
}