package specs

import testy.Spec

import scala.language.implicitConversions

class ImplicitLoggingSpec extends Spec {
  "implicit logger" should {
    "config properly" in {
      ImplicitLoggingTestObject.initialize()
    }
    "properly log a simple message" in {
      val line = Some(19)

      ImplicitLoggingTestObject.doSomething()
      ImplicitLoggingTestObject.testingModifier.records.length should be(1)
      val record = ImplicitLoggingTestObject.testingModifier.records.head
      record.className should be("specs.ImplicitLoggingTestObject")
      record.methodName should be(Some("doSomething"))
      record.line should be(line)
    }
  }
}