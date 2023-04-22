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
      val line = Some(14)

      ImplicitLoggingTestObject.doSomething()
      ImplicitLoggingTestObject.writer.records.length should be(1)
      val record = ImplicitLoggingTestObject.writer.records.head
      record.className should be("specs.ImplicitLoggingTestObject")
      record.methodName should be(Some("doSomething"))
      record.line should be(line)
    }
  }
}