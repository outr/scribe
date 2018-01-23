package scribe2

case class LogHandler(formatter: Formatter, writer: Writer, modifiers: List[LogModifier]) {
  def log(record: LogRecord): Unit = {
    modifiers.foldLeft(Option(record))((r, lm) => r.flatMap(lm.apply)).foreach { r =>
      writer.write(formatter.format(r))
    }
  }
}
