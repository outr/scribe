package spec

import scribe.Execution.global
import scribe.Position

import scala.concurrent.Future

object AsyncTesting {
  def position(): Future[List[Position]] = scribe.async {
    Future {
      Test1.one()
    }
  }.flatMap(identity)

  object Test1 {
    def one(): Future[List[Position]] = scribe.async {
      Future {
        Test2.two()
      }
    }.flatMap(identity)
  }

  object Test2 {
    def two(): Future[List[Position]] = scribe.async {
      Future {
        Test3.three()
      }
    }.flatMap(identity)
  }

  object Test3 {
    def three(): Future[List[Position]] = scribe.async {
      Future {
        throw new RuntimeException("Failure!")
      }
    }
  }
}
