package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.{Logger, ScribeConfig}

class ConfigSpec extends AnyWordSpec with Matchers {
  "ScribeConfig" should {
    "automatically load" in {
      Logger
      ScribeConfig.loaded should be(true)
    }
  }
}