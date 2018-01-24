package scribe

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scribe.writer.Writer

class AsynchronousSequentialWriter(writer: Writer) extends Writer {
  private lazy val router: ActorRef = AsynchronousSequentialWriter.system.actorOf(Props[Worker](new Worker))

  override def write(output: String): Unit = {
    router ! output
  }

  class Worker extends Actor {
    override def receive: Receive = {
      case output: String => writer.write(output)
    }
  }
}

object AsynchronousSequentialWriter {
  private lazy val system = {
    disposables += dispose
    ActorSystem("ScribeAsynchronousSequentialWriter")
  }

  def apply(writer: Writer): Writer = new AsynchronousSequentialWriter(writer)

  def dispose(): Unit = system.terminate()
}