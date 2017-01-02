package specs

import com.outr.scribe._

object ImplicitLoggingTestObject {
  val testingWriter = new TestingWriter

  def setup(): Unit = {
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
