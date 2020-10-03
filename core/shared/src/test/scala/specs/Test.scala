package specs

import scribe.{Level, LogRecord, Logger}
import scribe.format.Formatter
import scribe.handler.LogHandler
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

object Test {
  def main(args: Array[String]): Unit = {
    var logged = List.empty[String]

    def verify(expected: String*): Unit = try {
      println(s"Logged: $logged should be ${expected.toList}")
    } finally {
      logged = Nil
    }

    val parent = Logger().orphan().withMinimumLevel(Level.Error).withHandler(new LogHandler {
      override def formatter: Formatter = Formatter.classic

      override def writer: Writer = ConsoleWriter

      override def withFormatter(formatter: Formatter): LogHandler = ???

      override def withWriter(writer: Writer): LogHandler = ???

      override def modifiers: List[LogModifier] = Nil

      override def setModifiers(modifiers: List[LogModifier]): LogHandler = this

      override def log[M](record: LogRecord[M]): Unit = {
        logged = record.logOutput.plainText :: logged
      }
    }).replace()

    val child = Logger().withParent(parent).withMinimumLevel(Level.Info).replace()

    println(s"Parent ID: ${parent.id}, Child ID: ${child.id}")
    verify()
    parent.info("1")
    parent.error("2")
    verify("2")
    child.info("3")
    child.error("4")
    verify("4", "3")
  }
}
