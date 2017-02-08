package specs

import scribe._

object ImplicitLoggingTestObject {
  val testingWriter = new TestingWriter

  def initialize(): Unit = {
    logger.update {
      logger.copy(parentName = None)
    }
    val handler = LogHandler(level = Level.Debug, writer = testingWriter)
    logger.addHandler(handler)
  }

  def doSomething(): Unit = {
    logger.info("did something!")
  }
}
