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
  lazy val enhanced: Formatter = formatter"$dateCounter [$threadNameAbbreviated] ${string("[")}$levelColoredPaddedRight${string("]")} ${green(position)} - $message$mdc"
  lazy val advanced: Formatter = formatter"${bgBlue(bold(dateCounter))} ${italic(threadName)} $levelColored ${green(position)}$newLine${multiLine(message, mdc)}"
  lazy val strict: Formatter = formatter"$date [$threadNameAbbreviated] $levelPaddedRight $positionAbbreviated - $message$mdc"
  var default: Formatter = enhanced

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}