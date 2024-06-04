package specs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Logger
import scribe.mdc._
import scribe.writer.CacheWriter

class MDCSpec extends AnyWordSpec with Matchers {
  private lazy val writer = new CacheWriter
  private lazy val logger = Logger("mdc-test").orphan().withHandler(writer = writer)

  "MDC" should {
    "set a simple value to MDC and it get logged" in {
      MDC("test") = "simple value"
      try {
        logger.info("Simple test")
        writer.consume { list =>
          list.map(_.data("test")()) should be(List("simple value"))
        }
      } finally {
        MDC.remove("test")
      }
    }
    "use context to set and remove the value" in {
      val key = "contextualized"
      logger.info("One")
      MDC.context(key -> "testing") {
        logger.info("Two")
      }
      logger.info("Three")
      writer.consume { list =>
        list.map(r => r.messages.head.logOutput.plainText -> r.data.get(key).map(_())) should be(List(
          "Three" -> None,
          "Two" -> Some("testing"),
          "One" -> None
        ))
      }
    }
  }
}
