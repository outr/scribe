package specs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scribe.filter._
import scribe.{Level, Logger}
import scribe.writer.CacheWriter

class LoggingLevelFilteringSpec extends AnyWordSpec with Matchers {
  private lazy val errorWriter = new CacheWriter()
  private lazy val traceWriter = new CacheWriter()
  private lazy val debugWriter = new CacheWriter()

  private val pkg1 = "specs"
  private val pkg2 = "com.foo"

  "Logging Level Filtering" should {
    "configure the loggers" in {
      Logger.reset()
      Logger.root
        .clearHandlers()
        .withMinimumLevel(Level.Info)
        .withHandler(writer = errorWriter, minimumLevel = Some(Level.Error))
        .withHandler(
          writer = traceWriter,
          modifiers = List(
            select(packageName(pkg1), packageName(pkg2))
              .include(level === Level.Trace)
              .excludeUnselected
          )
        )
        .withHandler(writer = debugWriter, minimumLevel = Some(Level.Debug))
        .replace()
      Logger(pkg1).withMinimumLevel(Level.Trace).replace()
    }
    "verify an error gets logged" in {
      scribe.error("Error1")
      errorWriter.consumeMessages { list =>
        list should be(List("Error1"))
      }
      traceWriter.consumeMessages { list =>
        list should be(Nil)
      }
      debugWriter.consumeMessages { list =>
        list should be(List("Error1"))
      }
    }
    "verify a trace message gets logged" in {
      scribe.trace("Trace1")
      errorWriter.consumeMessages { list =>
        list should be(Nil)
      }
      traceWriter.consumeMessages { list =>
        list should be(List("Trace1"))
      }
      debugWriter.consumeMessages { list =>
        list should be(Nil)
      }
    }
    "reset the root logger" in {
      Logger.root.reset()
    }
  }
}
