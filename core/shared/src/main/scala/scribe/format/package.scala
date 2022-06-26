package scribe

import scribe.format.FormatBlock.{MultiLine, RawString}
import scribe.output._

import scala.collection.mutable.ListBuffer
import scala.language.experimental.macros
import scala.language.implicitConversions

package object format {
  protected val self = this

  private val ThreadNameAbbreviationLength = 10
  private val ClassNameAbbreviationLength = 15
  private val PositionAbbreviationLength = 25

  case object empty extends FormatBlock {
    override def format(record: LogRecord): LogOutput = EmptyOutput
  }
  lazy val space: FormatBlock = string(" ")
  lazy val openBracket: FormatBlock = string("[")
  lazy val closeBracket: FormatBlock = string("]")
  def string(value: String): FormatBlock = RawString(value)
  def date: FormatBlock = FormatBlock.Date.Standard
  def dateFull: FormatBlock = FormatBlock.Date.Full
  def dateCounter: FormatBlock = FormatBlock.Date.Counter
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
      case Level.Fatal => Color.Magenta
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
  def levelColor(block: FormatBlock): FormatBlock = FormatBlock { logRecord =>
    val color = logRecord.level match {
      case Level.Trace => Color.White
      case Level.Debug => Color.Green
      case Level.Info => Color.Blue
      case Level.Warn => Color.Yellow
      case Level.Error => Color.Red
      case Level.Fatal => Color.Magenta
      case _ => Color.Cyan
    }
    new ColoredOutput(color, block.format(logRecord))
  }
  def groupBySecond(blocks: FormatBlock*): FormatBlock = {
    var lastId: Long = 0L
    var lastThreadName: String = ""
    var lastTime: Long = 0L
    var lastLevel: Level = Level.Trace
    var lastClassName: String = ""
    var lastMethodName: Option[String] = None
    var lastLineNumber: Option[Int] = None
    var previousOutput: Option[LogOutput] = None
    FormatBlock { logRecord =>
      synchronized {
        val threadName = logRecord.thread.getName
        val distance = logRecord.timeStamp - lastTime
        val level = logRecord.level
        val cn = logRecord.className
        val mn = logRecord.methodName
        val ln = logRecord.line
        if (lastId == logRecord.id && previousOutput.nonEmpty) {
          previousOutput.getOrElse(sys.error("Previous output is None"))
        } else if (threadName == lastThreadName &&
          distance <= 1000L &&
          level == lastLevel &&
          cn == lastClassName &&
          mn == lastMethodName &&
          ln == lastLineNumber) {
          previousOutput = None
          EmptyOutput
        } else {
          lastId = logRecord.id
          lastThreadName = threadName
          lastTime = logRecord.timeStamp
          lastLevel = level
          lastClassName = cn
          lastMethodName = mn
          lastLineNumber = ln
          val output = new CompositeOutput(blocks.map(_.format(logRecord)).toList)
          previousOutput = Some(output)
          output
        }
      }
    }
  }
  def fileName: FormatBlock = FormatBlock.FileName
  def line: FormatBlock = FormatBlock.LineNumber
  def column: FormatBlock = FormatBlock.ColumnNumber
  def className: FormatBlock = FormatBlock.ClassName
  def classNameAbbreviated: FormatBlock = className.abbreviate(
    maxLength = ClassNameAbbreviationLength,
    padded = true
  )
  def classNameSimple: FormatBlock = FormatBlock.ClassNameSimple
  def methodName: FormatBlock = FormatBlock.MethodName
  def position: FormatBlock = FormatBlock.Position
  def positionAbbreviated: FormatBlock = FormatBlock.Position.abbreviate(maxLength = PositionAbbreviationLength)
  def positionSimple: FormatBlock = FormatBlock.PositionSimple
  def messages: FormatBlock = FormatBlock.Messages
  def newLine: FormatBlock = FormatBlock.NewLine
  def mdc(key: String,
          default: => Any = "",
          prefix: FormatBlock = empty,
          postfix: FormatBlock = empty): FormatBlock = {
    val pre = if (prefix == empty) None else Some(prefix)
    val post = if (postfix == empty) None else Some(postfix)
    FormatBlock.MDCReference(key, () => default, pre, post)
  }
  def mdc: FormatBlock = FormatBlock.MDCAll
  def mdcMultiLine: FormatBlock = FormatBlock.MDCMultiLine
  def multiLine(blocks: FormatBlock*): FormatBlock = new MultiLine(blocks = blocks.toList)

  implicit class EnhancedColor(color: Color) {
    def apply(block: FormatBlock): FormatBlock = fg(block)
    def fg(block: FormatBlock): FormatBlock = self.fg(color, block)
    def bg(block: FormatBlock): FormatBlock = self.bg(color, block)
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
    def formatter(args: Any*): Formatter = {
      val list = ListBuffer.empty[FormatBlock]
      val argsVector = args.toVector.asInstanceOf[Vector[FormatBlock]]
      sc.parts.zipWithIndex.foreach {
        case (part, index) => {
          if (part.nonEmpty) {
            list += string(part)
          }
          if (index < argsVector.size) {
            list += argsVector(index)
          }
        }
      }
      Formatter.fromBlocks(list.toList: _*)
    }
  }
}
