package spec

import scribe.Position

import scala.concurrent.Future
import scribe.Execution.global

object FutureTesting {
  def position(): Future[List[Position]] = {
    Future {
      Test1.doSomething()
    }.flatten
  }

  object Test1 {
    def doSomething(): Future[List[Position]] = {
      Future {
        Test2.doSomething()
      }.flatten
    }
  }

  object Test2 {
    def doSomething(): Future[List[Position]] = {
      Future {
        Test3.doSomething()
      }.flatten
    }
  }

  object Test3 {
    def doSomething(): Future[List[Position]] = {
      Future {
        Position.stack
      }
    }
  }
}