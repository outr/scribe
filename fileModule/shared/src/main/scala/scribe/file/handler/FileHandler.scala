package scribe.file.handler

import scribe.Priority
import scribe.file.FileWriter
import perfolation._
import scribe.util.Time

trait FileHandler {
  def apply(status: WriteStatus, writer: FileWriter): Unit

  def priority: Priority = Priority.Normal
}

object FileHandler {
  implicit final val FileHandlerOrdering: Ordering[FileHandler] = Ordering.by[FileHandler, Double](_.priority.value).reverse

  def before(priority: Priority = Priority.Normal)(f: FileWriter => Unit): FileHandler = {
    val p = priority

    new FileHandler {
      override def apply(status: WriteStatus, writer: FileWriter): Unit = if (status == WriteStatus.Before) {
        f(writer)
      }

      override def priority: Priority = p
    }
  }

  def after(priority: Priority = Priority.Normal)(f: FileWriter => Unit): FileHandler = {
    val p = priority

    new FileHandler {
      override def apply(status: WriteStatus, writer: FileWriter): Unit = if (status == WriteStatus.After) {
        f(writer)
      }

      override def priority: Priority = p
    }
  }

  def daily(f: FileWriter => Unit): FileWriter => Unit = {
    var previousDayOfYear = 0
    (writer: FileWriter) => {
      val currentDayOfYear = Time().t.dayOfYear
      if (currentDayOfYear != previousDayOfYear) {
        previousDayOfYear = currentDayOfYear
        f(writer)
      }
    }
  }
}