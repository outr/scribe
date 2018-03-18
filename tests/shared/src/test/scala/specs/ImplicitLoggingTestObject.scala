package specs

import scribe._
import scribe.handler.LogHandler
import scribe.writer.NullWriter

object ImplicitLoggingTestObject {
  val testingModifier = new TestingModifier

  def initialize(): Unit = {
    val handler = LogHandler(
      writer = NullWriter,
      modifiers = List(testingModifier)
    )
    this.logger.orphan().withHandler(handler).replace()
  }

  def doSomething(): Unit = {
    scribe.info("did something!")
  }
}
