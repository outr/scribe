package scribe.handler

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scribe._
import scribe.format.Formatter
import scribe.modify.LogModifier
import scribe.writer.{ConsoleWriter, Writer}

import scala.concurrent.Await
import scala.concurrent.duration._

case class AsynchronousLogHandler(formatter: Formatter = Formatter.default,
                                  writer: Writer = ConsoleWriter,
                                  modifiers: List[LogModifier] = Nil) extends LogHandler {
  private lazy val router: ActorRef = AsynchronousLogHandler.system.actorOf(Props[Worker](new Worker))

  def withFormatter(formatter: Formatter): LogHandler = copy(formatter = formatter)
  def withWriter(writer: Writer): LogHandler = copy(writer = writer)
  override def setModifiers(modifiers: List[LogModifier]): LogHandler = copy(modifiers = modifiers)

  override def log[M](record: LogRecord[M]): Unit = router ! record

  class Worker extends Actor {
    override def receive: Receive = {
      case record: LogRecord[_] => {
        modifiers.foldLeft(Option(record.asInstanceOf[LogRecord[Any]]))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
          writer.write(formatter.format(r))
        }
      }
    }
  }
}

object AsynchronousLogHandler {
  private lazy val system = {
    disposables += dispose _
    ActorSystem("AsynchronousLogHandler")
  }

  lazy val default: LogHandler = AsynchronousLogHandler()

  def apply(handler: SynchronousLogHandler): LogHandler = {
    AsynchronousLogHandler(handler.formatter, handler.writer, handler.modifiers)
  }

  def dispose(): Unit = {
    system.terminate()
    Await.ready(system.whenTerminated, 15.seconds)
  }
}