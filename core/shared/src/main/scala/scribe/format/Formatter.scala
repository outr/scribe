package scribe.format

import scribe.LogRecord

trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  lazy val simple: Formatter = formatter"$message$newLine"
  lazy val default: Formatter = formatter"$date [$threadName] $levelPaddedRight $position - $message$newLine"

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}