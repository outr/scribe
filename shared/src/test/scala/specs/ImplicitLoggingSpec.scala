package specs

import org.scalatest.{Matchers, WordSpec}

import scribe._

class ImplicitLoggingSpec extends WordSpec with Matchers {
  "implicit logger" should {
    "verify implicit logger has the correct name" in {
      logger.name should be(Some("specs.ImplicitLoggingSpec"))
    }
    "config properly" in {
      ImplicitLoggingTestObject.initialize()
    }
    "properly log a simple message" in {
      val lineNumber = 17

      ImplicitLoggingTestObject.doSomething()
      ImplicitLoggingTestObject.testingWriter.records.length should be(1)
      val record = ImplicitLoggingTestObject.testingWriter.records.head
      record.className should be("specs.ImplicitLoggingTestObject")
      record.methodName should be(Some("doSomething"))
      record.lineNumber should be(lineNumber)
    }
  }
}
