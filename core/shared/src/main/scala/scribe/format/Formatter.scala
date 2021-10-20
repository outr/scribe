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
  lazy val enhanced: Formatter = Formatter.fromBlocks(
    dateCounter,
    space,
    openBracket,
    threadNameAbbreviated,
    closeBracket,
    space,
    openBracket,
    levelColoredPaddedRight,
    closeBracket,
    space,
    green(position),
    string(" - "),
    message,
    mdc
  )
  lazy val advanced: Formatter = Formatter.fromBlocks(
    bgBlue(bold(dateCounter)),
    space,
    italic(threadName),
    space,
    levelColored,
    space,
    green(position),
    newLine,
    multiLine(message),
    mdcMultiLine
  )
  lazy val strict: Formatter = formatter"$date [$threadNameAbbreviated] $levelPaddedRight $positionAbbreviated - $message$mdc"
  var default: Formatter = enhanced

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}