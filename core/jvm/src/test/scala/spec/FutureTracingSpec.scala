package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class FutureTracingSpec extends AnyWordSpec with Matchers {
  "Future tracing" when {
    "using scribe implicits" should {
      "future trace back" in {
        val exception = intercept[RuntimeException](Await.result(FutureTesting.position(), Duration.Inf))
        val trace = exception.getStackTrace
        trace(0).getFileName should be("FutureTesting.scala")
        trace(0).getLineNumber should be(27)

        trace(1).getFileName should be("FutureTesting.scala")
        trace(1).getMethodName should be("three")
        trace(1).getLineNumber should be(26)

        trace(2).getFileName should be("FutureTesting.scala")
        trace(2).getMethodName should be("two")
        trace(2).getLineNumber should be(20)

        trace(3).getFileName should be("FutureTesting.scala")
        trace(3).getMethodName should be("one")
        trace(3).getLineNumber should be(14)

        trace(4).getFileName should be("FutureTesting.scala")
        trace(4).getMethodName should be("position")
        trace(4).getLineNumber should be(9)
      }
      "async trace back" in {
        val exception = intercept[RuntimeException](Await.result(AsyncTesting.position(), Duration.Inf))
        val trace = exception.getStackTrace

        var i = 0

        trace(i).getFileName should be("AsyncTesting.scala")
        trace(i).getLineNumber should be(34)
        i += 1

        trace(i).getFileName should be("AsyncTesting.scala")
        trace(i).getMethodName should be("three")
        trace(i).getLineNumber should be(32)
        i += 1

        if (trace(i).getMethodName == "three") {
          trace(i).getFileName should be("AsyncTesting.scala")
          trace(i).getMethodName should be("three")
          trace(i).getLineNumber should be(33)
          i += 1
        }

        trace(i).getFileName should be("AsyncTesting.scala")
        trace(i).getMethodName should be("two")
        trace(i).getLineNumber should be(25)
        i += 1

        trace(i).getFileName should be("AsyncTesting.scala")
        trace(i).getMethodName should be("one")
        trace(i).getLineNumber should be(17)
        i += 1

        trace(i).getFileName should be("AsyncTesting.scala")
        trace(i).getMethodName should be("position")
        trace(i).getLineNumber should be(10)
      }
    }
  }
}