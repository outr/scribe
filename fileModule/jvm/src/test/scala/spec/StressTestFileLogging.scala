package spec

import scribe._
import scribe.file._
import scribe.file.FileWriter

import java.nio.file.Files
import scala.concurrent.duration._

object StressTestFileLogging {
  def main(args: Array[String]): Unit = {
    val writer = FileWriter(
      "logs" / ("stress" % rolling("-" % year % "-" % month % "-" % day) % ".log"),
//      "logs" / ("stress" % maxSize(max = 1024 * 1024 * 5) % maxLogs(5, 15.seconds) % ".log"),
      append = false
    )
    val logger = Logger.empty.orphan().withHandler(writer = writer)
    val total = 10000000

    elapsed {
      (0 until total).foreach { index =>
        logger.info(s"Logging $index")
        if (index % 100000 == 0) {
          scribe.info(s"Logged $index records")
        }
      }
      scribe.info(s"Logged $total records!")
      writer.flush()
      scribe.info("Flushed!")
    }
    val path = writer.path
    writer.dispose()
    val lines = Files.lines(path).count()
    scribe.info(s"Lines: $lines")
    Files.delete(path)
  }
}