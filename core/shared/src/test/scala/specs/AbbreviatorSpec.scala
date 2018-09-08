package specs

import org.scalatest.{Matchers, WordSpec}
import scribe.util.Abbreviator

class AbbreviatorSpec extends WordSpec with Matchers {
  "Abbreviator" should {
    val className1 = "mainPackage.sub.sample.Bar"
    val className2 = "mainPackage.sub.sample.FooBar"

    "properly abbreviate 26 length" in {
      val s = Abbreviator(className1, 26)
      s should be(className1)
    }
    "properly abbreviate 16 length" in {
      val s = Abbreviator(className1, 16)
      s should be("m.sub.sample.Bar")
    }
    "properly abbreviate 15 length" in {
      val s = Abbreviator(className1, 15)
      s should be("m.s.sample.Bar")
    }
    "properly abbreviate 10 length" in {
      val s = Abbreviator(className1, 10)
      s should be("m.s.s.Bar")
    }
    "properly abbreviate 5 length" in {
      val s = Abbreviator(className1, 5)
      s should be("Bar")
    }
    "properly abbreviate 0 length" in {
      val s = Abbreviator(className1, 0)
      s should be("Bar")
    }
    "properly abbreviate longer class name at 5" in {
      val s = Abbreviator(className2, 5, abbreviateName = true)
      s should be("Fo...")
    }
  }
}
