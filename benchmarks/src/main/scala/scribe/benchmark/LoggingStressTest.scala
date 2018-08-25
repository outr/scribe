package scribe.benchmark

import scribe.Logger

object LoggingStressTest {
  def main(args: Array[String]): Unit = {
    val logger = Logger.empty.orphan()
    while (true) {
      logger.info("This is a test")
    }
  }
}