package spec

import scribe.{Logger, ScribeConfig}
import testy.Spec

class ConfigSpec extends Spec {
  "ScribeConfig" should {
    "automatically load" in {
      Logger
      ScribeConfig.loaded should be(true)
    }
  }
}