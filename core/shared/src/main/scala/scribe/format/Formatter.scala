package scribe.format

import scribe.LogRecord

trait Formatter {
  def format[M](record: LogRecord[M]): String
}

object Formatter {
  lazy val simple: Formatter = formatter"$message$mdc$newLine"
  lazy val default: Formatter = formatter"$date $level $position - $message$mdc$newLine"
  lazy val strict: Formatter = formatter"$date [$threadNameAbbreviated] $levelPaddedRight $positionAbbreviated - $message$mdc$newLine"

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}