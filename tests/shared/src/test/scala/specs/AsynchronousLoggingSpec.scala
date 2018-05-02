package specs

import java.util.concurrent.ConcurrentLinkedQueue

import org.scalatest.{AsyncWordSpec, Matchers}
import scribe.Logger
import scribe.format._
import scribe.writer.Writer

import scala.concurrent.Future
import scala.collection.JavaConverters._
import perfolation._

class AsynchronousLoggingSpec extends AsyncWordSpec with Matchers {
  "Asynchronous Logging" should {
    val queue = new ConcurrentLinkedQueue[String]
    val logger = Logger.empty.orphan().withHandler(
      formatter = AsynchronousLoggingSpec.format,
      writer = new Writer {
        override def write(output: String): Unit = queue.add(output)
      }
    )

    "log 10000 records in the proper order" in {
      val Regex = """(\d+) - (.+)""".r

      Future.sequence("abcdefghij".map { char =>
        Future {
          (0 until 1000).foreach { index =>
            logger.info(p"$char:$index")
          }
        }
      }).map { _ =>
        var previous = 0L
        queue.iterator().asScala.foreach {
          case Regex(ts, message) => {
            val timeStamp = ts.toLong
            timeStamp should be >= previous
            previous = timeStamp
          }
        }
        queue.size() should be(10000)
      }
    }
  }
}

object AsynchronousLoggingSpec {
  val format = formatter"$timeStamp - $message"
}