package spec

import org.scalatest.{AsyncWordSpec, Matchers}
import scribe.Position

import scala.concurrent.Future

class FutureTracingSpec extends AsyncWordSpec with Matchers {
  "Future tracing" when {
//    "using standard implicits" should {
//      "not include any trace back" in {
//        FutureTesting.default().map { stack =>
//          stack.length should be(0)
//        }
//      }
//    }
    "using scribe implicits" should {
      "include trace back" in {
        FutureTesting.position().map { stack =>
          scribe.info(stack)
          stack.length should be(4)
        }
      }
    }
  }
}