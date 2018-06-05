package spec

import scribe.Position

import scala.concurrent.Future
import scribe.Execution.global

object FutureTesting {
  def position(): Future[List[Position]] = scribe.future {
    Test1.one()
  }.flatMap(identity)

  object Test1 {
    def one(): Future[List[Position]] = scribe.future {
      Test2.two()
    }.flatMap(identity)
  }

  object Test2 {
    def two(): Future[List[Position]] = scribe.future {
      Test3.three()
    }.flatMap(identity)
  }

  object Test3 {
    def three(): Future[List[Position]] = scribe.future {
      throw new RuntimeException("Failure!")
    }
  }
}