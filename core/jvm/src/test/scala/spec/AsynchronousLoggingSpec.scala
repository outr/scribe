package spec

import java.util.concurrent.ConcurrentLinkedQueue
import scribe.format._
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer
import scribe.{LogRecord, Logger}
import testy.Spec

import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

import scribe.Execution.global

class AsynchronousLoggingSpec extends Spec {
  private val Regex = """(\d+) - (.+)""".r
  private val threads = "abcdefghijklmnopqrstuvwxyz"
  private val iterations = 10
  private val total = threads.length * iterations

  "Asynchronous Logging" should {
    s"log $total records in the proper order with simple logging" in {
      val queue = new ConcurrentLinkedQueue[String]
      val logger = Logger.empty.orphan().withHandler(
        formatter = AsynchronousLoggingSpec.format,
        writer = new Writer {
          override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = queue.add(output.plainText.trim)
        }
      )

      Future.sequence(threads.map { char =>
        Future {
          (0 until iterations).foreach { index =>
            logger.info(s"$char:$index")
          }
        }
      }).map { _ =>
        var previous = 0L
        queue.iterator().asScala.foreach {
          case Regex(ts, _) => {
            val timeStamp = ts.toLong
            timeStamp should be >= previous
            previous = timeStamp
          }
        }
        queue.size() should be(total)
      }
    }
    /*s"log $total records in the proper order with file logging" in {
      val file = new File("logs/app.log")
      file.delete()

      val fileWriter = FileWriter().nio
      val logger = Logger.empty.orphan().withHandler(
        formatter = AsynchronousLoggingSpec.format,
        writer = fileWriter,
        outputFormat = ASCIIOutputFormat
      )

      Future.sequence(threads.map { char =>
        Future {
          (0 until iterations).foreach { index =>
            logger.info(s"$char:$index")
          }
        }
      }).map { _ =>
        var previous = 0L
        fileWriter.flush()
        fileWriter.dispose()
        val source = Source.fromFile(file)
        try {
          val lines = source.getLines().toList
          lines.foreach {
            case Regex(ts, message) => {
              val timeStamp = ts.toLong
              timeStamp should be >= previous
              previous = timeStamp
            }
          }
          lines.length should be(threads.length * iterations)
        } finally {
          source.close()
        }
      }
    }*/
  }
}

object AsynchronousLoggingSpec {
  val format = formatter"$timeStamp - $message"
}