package spec

import scribe.Log4JMigration
import testy.Spec

import scala.language.implicitConversions

class Log4JMigrationSpec extends Spec {
  "Log4JMigration" should {
    "load existing configuration" in {
      Log4JMigration() should be(10)
    }
  }
}