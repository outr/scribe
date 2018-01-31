package specs

import org.scalatest.{Matchers, WordSpec}

class ImplicitLoggingSpec extends WordSpec with Matchers {
  "implicit logger" should {
    "config properly" in {
      ImplicitLoggingTestObject.initialize()
    }
    "properly log a simple message" in {
      val lineNumber = Some(15)

      ImplicitLoggingTestObject.doSomething()
      ImplicitLoggingTestObject.testingModifier.records.length should be(1)
      val record = ImplicitLoggingTestObject.testingModifier.records.head
      record.className should be("specs.ImplicitLoggingTestObject")
      record.methodName should be(Some("doSomething"))
      record.lineNumber should be(lineNumber)
    }
  }
}
