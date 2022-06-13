package scribe.format

import scribe.LogRecord
import scribe.output.LogOutput

trait Formatter {
  def format(record: LogRecord): LogOutput
}

object Formatter {
  /**
   * Only includes the log message and MDC
   */
  lazy val simple: Formatter = formatter"$messages$mdc"
  /**
   * Only includes the log message and MDC, but the message is colored based on the logging level
   */
  lazy val colored: Formatter = formatter"${levelColor(messages)}$mdc"
  /**
   * A classic logging style including the date, thread name (abbreviated), level, position, message, and MDC
   */
  lazy val classic: Formatter = formatter"$date [$threadNameAbbreviated] $level $position - $messages$mdc"
  /**
   * Colored, but more compact output to show more on a single line
   */
  lazy val compact: Formatter = formatter"$date ${string("[")}$levelColored${string("]")} ${green(position)} - $messages$mdc"
  /**
   * A rich log output format with coloring and lots of details. The default format.
   */
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
    messages,
    mdc
  )
  /**
   * A multi-line formatter that includes expanded log information on the first line, and indented and auto-wrapping
   * message and MDC on the following line(s).
   */
  lazy val advanced: Formatter = Formatter.fromBlocks(
    groupBySecond(
      cyan(bold(dateFull)),
      space,
      italic(threadName),
      space,
      levelColored,
      space,
      green(position),
      newLine
    ),
    multiLine(messages),
    mdcMultiLine
  )
  /**
   * A strict format with a focus on consistent width.
   */
  lazy val strict: Formatter = formatter"$date [$threadNameAbbreviated] $levelPaddedRight $positionAbbreviated - $messages$mdc"
  /**
   * The default formatter. This is used as a default when the formatter isn't explicitly specified. Defaults to
   * enhanced.
   */
  var default: Formatter = advanced

  def fromBlocks(blocks: FormatBlock*): Formatter = new FormatBlocksFormatter(blocks.toList)
}