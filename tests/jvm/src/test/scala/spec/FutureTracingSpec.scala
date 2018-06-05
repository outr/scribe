package spec

import java.lang.Thread.UncaughtExceptionHandler

import org.scalatest.{AsyncWordSpec, Matchers, WordSpec}
import scribe.Position

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class FutureTracingSpec extends WordSpec with Matchers {
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
        try {
          val result = Await.result(FutureTesting.position(), Duration.Inf)
          println(result)
        } catch {
          case t: Throwable => {
            println(s"CAUGHT! ${t.getMessage} / ${Position.stack}")
            t.printStackTrace()
          }
        }
      }
    }
  }
}