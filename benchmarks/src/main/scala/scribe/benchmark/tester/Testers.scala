package scribe.benchmark.tester

class Testers {
  val log4cats = new Log4CatsLoggingTester
  val log4j = new Log4JLoggingTester
  val log4jTrace = new Log4JTraceLoggingTester
  val log4s = new Log4SLoggingTester
  val logback = new LogbackLoggingTester
  val scalaLogging = new ScalaLoggingLoggingTester
  val scribeAsync = new ScribeAsyncLoggingTester
  val scribeEffect = new ScribeEffectLoggingTester
  val scribeEffectParallel = new ScribeEffectParallelLoggingTester
  val scribe = new ScribeLoggingTester
  val tinyLog = new TinyLogLoggingTester

  val all: List[LoggingTester] = List(
    scribe, scribeAsync, scribeEffect, scribeEffectParallel,
    log4cats, log4s, scalaLogging,
    log4j, log4jTrace, logback, tinyLog
  )
}
