package scribe2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

trait AsynchronousSequentialWriter extends Writer {
  private lazy val router: ActorRef = AsynchronousSequentialWriter.system.actorOf(Props[Worker](new Worker))

  override def write(output: String): Unit = {
    router ! output
  }

  protected def asyncWrite(output: String): Unit

  class Worker extends Actor {
    override def receive: Receive = {
      case output: String => asyncWrite(output)
    }
  }
}

object AsynchronousSequentialWriter {
  private lazy val system = ActorSystem("ScribeAsynchronousSequentialWriter")

  def dispose(): Unit = system.terminate()
}