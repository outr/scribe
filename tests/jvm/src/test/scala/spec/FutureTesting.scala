package spec

import scribe.Position

import scala.concurrent.Future

object FutureTesting {
  def position(): Future[List[Position]] = scribe.future {
    Test1.one()
  }.flatten

  object Test1 {
    def one(): Future[List[Position]] = scribe.future {
      Test2.two()
    }.flatten
  }

  object Test2 {
    def two(): Future[List[Position]] = scribe.future {
      Test3.three()
    }.flatten
  }

  object Test3 {
    def three(): Future[List[Position]] = scribe.future {
      throw new RuntimeException("Failure!")
    }
  }
}