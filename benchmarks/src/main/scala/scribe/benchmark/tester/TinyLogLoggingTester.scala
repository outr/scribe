package scribe.benchmark.tester

import org.pmw.tinylog

class TinyLogLoggingTester extends LoggingTester {
  override def init(): Unit = tinylog.Configurator
    .defaultConfig()
    .removeAllWriters()
    .level(tinylog.Level.INFO)
    .formatPattern("[{thread}] {class}.{method}(){level}: {message}")
    .writer(new tinylog.writers.FileWriter("logs/tiny.log"))
    .activate()

  override def run(messages: Iterator[String]): Unit = {
    messages.foreach(tinylog.Logger.info)
  }
}