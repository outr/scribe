package scribe

import scribe.format.FormatBlock.RawString
import scribe.output.{BackgroundColoredOutput, BoldOutput, Color, ColoredOutput, ItalicOutput, StrikethroughOutput, URLOutput, UnderlineOutput}

import scala.language.experimental.macros
import scala.language.implicitConversions

package object format {
  private val ThreadNameAbbreviationLength = 10
  private val ClassNameAbbreviationLength = 15
  private val PositionAbbreviationLength = 25

  def string(value: String): FormatBlock = RawString(value)
  def date: FormatBlock = FormatBlock.Date.Standard
  def dateFull: FormatBlock = FormatBlock.Date.Full
  def timeStamp: FormatBlock = FormatBlock.TimeStamp
  def time: FormatBlock = FormatBlock.Time
  def threadName: FormatBlock = FormatBlock.ThreadName
  def threadNameAbbreviated: FormatBlock = threadName.abbreviate(
    maxLength = ThreadNameAbbreviationLength,
    padded = true,
    abbreviateName = true
  )
  def level: FormatBlock = FormatBlock.Level
  def levelColored: FormatBlock = FormatBlock { logRecord =>
    val color = logRecord.level match {
      case Level.Trace => Color.White
      case Level.Debug => Color.Green
      case Level.Info => Color.Blue
      case Level.Warn => Color.Yellow
      case Level.Error => Color.Red
      case _ => Color.Cyan
    }
    new ColoredOutput(color, level.format(logRecord))
  }
  def levelPaddedRight: FormatBlock = FormatBlock.Level.PaddedRight
  def levelColoredPaddedRight: FormatBlock = FormatBlock { logRecord =>
    val color = logRecord.level match {
      case Level.Trace => Color.White
      case Level.Debug => Color.Green
      case Level.Info => Color.Blue
      case Level.Warn => Color.Yellow
      case Level.Error => Color.Red
      case _ => Color.Cyan
    }
    new ColoredOutput(color, FormatBlock.Level.PaddedRight.format(logRecord))
  }
  def fileName: FormatBlock = FormatBlock.FileName
  def line: FormatBlock = FormatBlock.LineNumber
  def column: FormatBlock = FormatBlock.ColumnNumber
  def className: FormatBlock = FormatBlock.ClassName
  def classNameAbbreviated: FormatBlock = className.abbreviate(
    maxLength = ClassNameAbbreviationLength,
    padded = true
  )
  def methodName: FormatBlock = FormatBlock.MethodName
  def position: FormatBlock = FormatBlock.Position
  def positionAbbreviated: FormatBlock = position.abbreviate(
    maxLength = PositionAbbreviationLength,
    padded = true,
    abbreviateName = true
  )
  def message: FormatBlock = FormatBlock.Message
  def newLine: FormatBlock = FormatBlock.NewLine
  def mdc(key: String): FormatBlock = FormatBlock.MDCReference(key)
  def mdc: FormatBlock = FormatBlock.MDCAll

  implicit class EnhancedColor(color: Color) {
    def apply(block: FormatBlock): FormatBlock = fg(block)
    def fg(block: FormatBlock): FormatBlock = format.fg(color, block)
    def bg(block: FormatBlock): FormatBlock = format.bg(color, block)
  }

  def color(color: Color, block: FormatBlock): FormatBlock = fg(color, block)

  def fg(color: Color, block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    new ColoredOutput(color, block.format(logRecord))
  }

  def bg(color: Color, block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    new BackgroundColoredOutput(color, block.format(logRecord))
  }

  def black(block: FormatBlock): FormatBlock = fg(Color.Black, block)
  def blue(block: FormatBlock): FormatBlock = fg(Color.Blue, block)
  def cyan(block: FormatBlock): FormatBlock = fg(Color.Cyan, block)
  def green(block: FormatBlock): FormatBlock = fg(Color.Green, block)
  def magenta(block: FormatBlock): FormatBlock = fg(Color.Magenta, block)
  def red(block: FormatBlock): FormatBlock = fg(Color.Red, block)
  def white(block: FormatBlock): FormatBlock = fg(Color.White, block)
  def yellow(block: FormatBlock): FormatBlock = fg(Color.Yellow, block)
  def gray(block: FormatBlock): FormatBlock = fg(Color.Gray, block)
  def brightBlue(block: FormatBlock): FormatBlock = fg(Color.BrightBlue, block)
  def brightCyan(block: FormatBlock): FormatBlock = fg(Color.BrightCyan, block)
  def brightGreen(block: FormatBlock): FormatBlock = fg(Color.BrightGreen, block)
  def brightMagenta(block: FormatBlock): FormatBlock = fg(Color.BrightMagenta, block)
  def brightRed(block: FormatBlock): FormatBlock = fg(Color.BrightRed, block)
  def brightWhite(block: FormatBlock): FormatBlock = fg(Color.BrightWhite, block)
  def brightYellow(block: FormatBlock): FormatBlock = fg(Color.BrightYellow, block)

  def bgBlack(block: FormatBlock): FormatBlock = bg(Color.Black, block)
  def bgBlue(block: FormatBlock): FormatBlock = bg(Color.Blue, block)
  def bgCyan(block: FormatBlock): FormatBlock = bg(Color.Cyan, block)
  def bgGreen(block: FormatBlock): FormatBlock = bg(Color.Green, block)
  def bgMagenta(block: FormatBlock): FormatBlock = bg(Color.Magenta, block)
  def bgRed(block: FormatBlock): FormatBlock = bg(Color.Red, block)
  def bgWhite(block: FormatBlock): FormatBlock = bg(Color.White, block)
  def bgYellow(block: FormatBlock): FormatBlock = bg(Color.Yellow, block)
  def bgGray(block: FormatBlock): FormatBlock = bg(Color.Gray, block)
  def bgBrightBlue(block: FormatBlock): FormatBlock = bg(Color.BrightBlue, block)
  def bgBrightCyan(block: FormatBlock): FormatBlock = bg(Color.BrightCyan, block)
  def bgBrightGreen(block: FormatBlock): FormatBlock = bg(Color.BrightGreen, block)
  def bgBrightMagenta(block: FormatBlock): FormatBlock = bg(Color.BrightMagenta, block)
  def bgBrightRed(block: FormatBlock): FormatBlock = bg(Color.BrightRed, block)
  def bgBrightWhite(block: FormatBlock): FormatBlock = bg(Color.BrightWhite, block)
  def bgBrightYellow(block: FormatBlock): FormatBlock = bg(Color.BrightYellow, block)

  def url(url: String, block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    new URLOutput(url, block.format(logRecord))
  }
  def bold(block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    new BoldOutput(block.format(logRecord))
  }
  def italic(block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    new ItalicOutput(block.format(logRecord))
  }
  def underline(block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    new UnderlineOutput(block.format(logRecord))
  }
  def strikethrough(block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    new StrikethroughOutput(block.format(logRecord))
  }

  implicit class FormatterInterpolator(val sc: StringContext) extends AnyVal {
    def formatter(args: Any*): Formatter = macro ScribeMacros.formatter
  }
}
