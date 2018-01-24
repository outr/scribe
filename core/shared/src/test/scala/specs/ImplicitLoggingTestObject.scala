package specs

import scribe.{Level, LogHandler}

object ImplicitLoggingTestObject {
  val testingWriter = new TestingWriter

  def initialize(): Unit = {
    val handler = LogHandler(level = Level.Debug, writer = testingWriter)
    scribe.addHandler(handler)
  }

  def doSomething(): Unit = {
    scribe.info("did something!")
  }
}
