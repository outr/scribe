package specs

import scribe._
import scribe.handler.LogHandler
import scribe.writer.NullWriter

object ImplicitLoggingTestObject {
  val handler = new TestingHandler

  def initialize(): Unit = {
    this.logger.orphan().withHandler(handler).replace()
  }

  def doSomething(): Unit = {
    scribe.info("did something!")
  }
}
