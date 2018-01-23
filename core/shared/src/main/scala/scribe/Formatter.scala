package scribe2

trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  lazy val simple: Formatter = formatter"$message$newLine"
  lazy val default: Formatter = formatter"$date [$threadName] $levelPaddedRight $positionAbbreviated - $message$newLine"

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}

class FormatBlocksFormatter(blocks: List[FormatBlock]) extends Formatter {
  override def format(record: LogRecord): String = {
    val b = new StringBuilder
    blocks.foreach(_.format(record, b))
    b.toString()
  }
}