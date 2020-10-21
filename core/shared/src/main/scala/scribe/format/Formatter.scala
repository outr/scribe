package scribe.format

import scribe._
import scribe.output.LogOutput

trait Formatter {
  def format[M](record: LogRecord[M]): LogOutput
}

object Formatter {
  lazy val simple: Formatter = formatter"$message$mdc"
  lazy val classic: Formatter = formatter"$date [$threadNameAbbreviated] $level $position - $message$mdc"
  lazy val compact: Formatter = formatter"$date ${string("[")}$levelColored${string("]")} ${green(position)} - $message$mdc"
  lazy val enhanced: Formatter = formatter"$dateFull ${string("[")}$levelColoredPaddedRight${string("]")} ${green(position)} - ${gray(message)}$mdc"
  lazy val strict: Formatter = formatter"$date [$threadNameAbbreviated] $levelPaddedRight $positionAbbreviated - $message$mdc"

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}