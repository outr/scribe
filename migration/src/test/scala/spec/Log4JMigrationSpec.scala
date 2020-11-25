package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.Log4JMigration

import scala.language.implicitConversions

class Log4JMigrationSpec extends AnyWordSpec with Matchers {
  "Log4JMigration" should {
    "load existing configuration" in {
      Log4JMigration() should be(10)
    }
  }
}