package spec

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.Await

class FutureTracingSpec extends WordSpec with Matchers {
  "Future tracing" when {
    "using scribe implicits" should {
      "include trace back" in {
        val exception = intercept[RuntimeException](Await.result(FutureTesting.position(), Duration.Inf))
        val trace = exception.getStackTrace
        trace(0).getFileName should be("FutureTesting.scala")
        trace(0).getMethodName should be("$anonfun$three$1")
        trace(0).getLineNumber should be(26)

        trace(1).getFileName should be("FutureTesting.scala")
        trace(1).getMethodName should be("three")
        trace(1).getLineNumber should be(25)

        trace(2).getFileName should be("FutureTesting.scala")
        trace(2).getMethodName should be("two")
        trace(2).getLineNumber should be(19)

        trace(3).getFileName should be("FutureTesting.scala")
        trace(3).getMethodName should be("one")
        trace(3).getLineNumber should be(13)

        trace(4).getFileName should be("FutureTesting.scala")
        trace(4).getMethodName should be("position")
        trace(4).getLineNumber should be(8)

        trace(5).getFileName should be("Future.scala")
      }
    }
  }
}