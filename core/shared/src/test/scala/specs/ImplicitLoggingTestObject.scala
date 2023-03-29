package specs

import scribe._
import scribe.writer.CacheWriter

object ImplicitLoggingTestObject {
  val writer = new CacheWriter()

  def initialize(): Unit = {
    this.logger.orphan().withHandler(writer = writer).replace()
  }

  def doSomething(): Unit = {
    scribe.info("did something!")
  }
}
