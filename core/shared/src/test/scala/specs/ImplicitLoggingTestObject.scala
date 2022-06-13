package specs

import scribe._

object ImplicitLoggingTestObject {
  val handler = new TestingHandler

  def initialize(): Unit = {
    this.logger.orphan().withHandler(handler).replace()
  }

  def doSomething(): Unit = {
    scribe.info("did something!")
  }
}
