package specs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.implicitConversions

class ImplicitLoggingSpec extends AnyWordSpec with Matchers {
  "implicit logger" should {
    "config properly" in {
      ImplicitLoggingTestObject.initialize()
    }
    "properly log a simple message" in {
      val line = Some(13)

      ImplicitLoggingTestObject.doSomething()
      ImplicitLoggingTestObject.handler.records.length should be(1)
      val record = ImplicitLoggingTestObject.handler.records.head
      record.className should be("specs.ImplicitLoggingTestObject")
      record.methodName should be(Some("doSomething"))
      record.line should be(line)
    }
  }
}