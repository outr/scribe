package specs

import scribe._
import scribe.handler.LogHandler
import scribe.writer.NullWriter

object ImplicitLoggingTestObject {
  val testingModifier = new TestingModifier

  def initialize(): Unit = {
    val handler = LogHandler(
      writer = NullWriter,
      minimumLevel = Level.Debug,
      modifiers = List(testingModifier)
    )
    this.updateLogger(_.orphan().withHandler(handler))
  }

  def doSomething(): Unit = {
    scribe.info("did something!")
  }
}
