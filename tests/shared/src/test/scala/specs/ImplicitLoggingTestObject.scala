package specs

import scribe._
import scribe.writer.NullWriter

object ImplicitLoggingTestObject {
  val testingModifier = new TestingModifier

  def initialize(): Unit = {
    val handler = LogHandler.default.withMinimumLevel(Level.Debug).withModifier(testingModifier).withWriter(NullWriter)
    this.updateLogger(_.orphan().withHandler(handler))
  }

  def doSomething(): Unit = {
    scribe.info("did something!")
  }
}
