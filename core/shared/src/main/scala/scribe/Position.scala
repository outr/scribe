package scribe

import scala.language.experimental.macros

case class Position(className: String,
                    methodName: Option[String],
                    line: Option[Int],
                    column: Option[Int],
                    fileName: String) {
  def toTraceElement: StackTraceElement = {
    val fn = if (fileName.indexOf('/') != -1) {
      fileName.substring(fileName.lastIndexOf('/') + 1)
    } else {
      fileName
    }
    new StackTraceElement(className, methodName.getOrElse("unknown"), fn, line.getOrElse(-1))
  }

  override def toString: String = {
    val mn = methodName.map(m => s":$m").getOrElse("")
    val ln = line.map(l => s":$l").getOrElse("")
    val cn = column.map(c => s":$c").getOrElse("")
    val fn = if (fileName.indexOf('/') != -1) {
      fileName.substring(fileName.lastIndexOf('/') + 1)
    } else {
      fileName
    }
    s"$className$mn$ln$cn ($fn)"
  }
}

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

  def stack: List[Position] = threadLocal.get().distinct

  def stack_=(stack: List[Position]): Unit = threadLocal.set(stack)

  def fix[T <: Throwable](throwable: T): T = {
    val positionTrace = stack.reverse.map(_.toTraceElement).distinct
    val original = throwable.getStackTrace.toList.filterNot(positionTrace.contains)
    val trace = original.head :: positionTrace ::: original.tail
    throwable.setStackTrace(trace.toArray)
    throwable
  }
}