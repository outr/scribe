package scribe

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

case class AsynchronousLogHandler(formatter: Formatter = Formatter.default,
                                  writer: Writer = ConsoleWriter,
                                  modifiers: List[LogModifier] = Nil) extends LogHandler {
  private lazy val router: ActorRef = AsynchronousLogHandler.system.actorOf(Props[Worker](new Worker))

  override def withFormatter(formatter: Formatter): LogHandler = copy(formatter = formatter)
  override def withWriter(writer: Writer): LogHandler = copy(writer = writer)
  override def withModifier(modifier: LogModifier): LogHandler = copy(modifiers = modifiers ::: List(modifier))
  override def withoutModifier(modifier: LogModifier): LogHandler = copy(modifiers = modifiers.filterNot(_ == modifier))

  override def log(record: LogRecord): Unit = router ! record

  class Worker extends Actor {
    override def receive: Receive = {
      case record: LogRecord => {
        modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
          writer.write(formatter.format(r))
        }
      }
    }
  }
}

object AsynchronousLogHandler {
  private lazy val system = {
    disposables += dispose
    ActorSystem("AsynchronousLogHandler")
  }

  lazy val default: LogHandler = AsynchronousLogHandler()

  def apply(handler: SynchronousLogHandler): LogHandler = {
    AsynchronousLogHandler(handler.formatter, handler.writer, handler.modifiers)
  }

  def dispose(): Unit = system.terminate()
}