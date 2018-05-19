package scribe

import scala.language.experimental.macros

case class Position(className: String,
                    methodName: Option[String],
                    line: Option[Int],
                    column: Option[Int],
                    fileName: String)

object Position {
  private lazy val threadLocal = new ThreadLocal[List[Position]] {
    override def initialValue(): List[Position] = Nil
  }

  def push(position: Position): Unit = threadLocal.set(position :: threadLocal.get())

  def push(): Unit = macro Macros.pushPosition

  def pop(): Option[Position] = {
    val stack = threadLocal.get()
    if (stack.nonEmpty) {
      threadLocal.set(stack.tail)
    }
    stack.headOption
  }

  def stack: List[Position] = threadLocal.get()

  def stack_=(stack: List[Position]): Unit = threadLocal.set(stack)
}