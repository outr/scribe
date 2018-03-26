package scribe.format

import scribe.LogRecord

trait Formatter {
  def format[M](record: LogRecord[M]): String
}

object Formatter {
  lazy val simple: Formatter = formatter"$message$newLine"
  lazy val default: Formatter = {
    formatter"$date [$threadName] $levelPaddedRight ${position.abbreviate(25)} - $message$newLine"
  }

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}