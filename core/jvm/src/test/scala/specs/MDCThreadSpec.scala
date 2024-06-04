package specs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.mdc._
import scribe.writer.CacheWriter

class MDCThreadSpec extends AnyWordSpec with Matchers {
  private lazy val writer = new CacheWriter
  private lazy val logger = Logger("mdc-test").orphan().withHandler(writer = writer)

  "MDC" should {
    "verify concurrency access to implicits" in {
      implicit val mdc: MDC = MDC.instance
      logger.info("Zero")
      mdc.context("test" -> "testing") {
        logger.info("One")
        mdc.context("test" -> "testing2") {
          logger.info("Two")
          var finished = false
          new Thread {
            override def run(): Unit = {
              logger.info("Three")
              finished = true
            }
          }.start()
          while (!finished) {
            Thread.sleep(10)
          }
        }
        logger.info("Four")
      }
      logger.info("Five")
      writer.consume { list =>
        list.map(r => r.messages.head.logOutput.plainText -> r.data.get("test").map(_())) should be(List(
          "Five" -> None,
          "Four" -> Some("testing"),
          "Three" -> Some("testing2"),
          "Two" -> Some("testing2"),
          "One" -> Some("testing"),
          "Zero" -> None
        ))
      }
    }
  }
}
