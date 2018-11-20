package scribe.writer

import java.io.PrintStream

import scribe._
import scribe.output._

object ConsoleWriter extends Writer {
  private object ansi {
    var fg: Option[ANSI] = None
    var bg: Option[ANSI] = None
  }

  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    val stream = if (record.level <= Level.Info) {
      Logger.system.out
    } else {
      Logger.system.err
    }
    writeOutput(output, stream)
  }

  private def writeOutput(output: LogOutput, stream: PrintStream): Unit = output match {
    case o: TextOutput => stream.print(o.plainText)
    case o: CompositeOutput => o.entries.foreach(writeOutput(_, stream))
    case o: ColoredOutput => {
      val color = color2ANSI(o.color)
      stream.print(color.ansi)
      val previous = ansi.fg
      ansi.fg = Some(color)
      try {
        writeOutput(o.output, stream)
      } finally {
        ansi.fg = previous
        stream.print(ansi.fg.map(_.ansi).getOrElse(color.default))
      }
    }
    case _ => stream.print(output.plainText)      // TODO: support warning unsupported
  }

  private def color2ANSI(color: Color): ANSI = color match {
    case Color.Black => ANSI.fg.Black
    case Color.Blue => ANSI.fg.Blue
    case Color.Cyan => ANSI.fg.Cyan
    case Color.Green => ANSI.fg.Green
    case Color.Magenta => ANSI.fg.Magenta
    case Color.Red => ANSI.fg.Red
    case Color.White => ANSI.fg.White
    case Color.Yellow => ANSI.fg.Yellow
    case Color.Gray => ANSI.fg.Gray
    case Color.BrightBlue => ANSI.fg.BrightBlue
    case Color.BrightCyan => ANSI.fg.BrightCyan
    case Color.BrightGreen => ANSI.fg.BrightGreen
    case Color.BrightMagenta => ANSI.fg.BrightMagenta
    case Color.BrightRed => ANSI.fg.BrightRed
    case Color.BrightWhite => ANSI.fg.BrightWhite
    case Color.BrightYellow => ANSI.fg.BrightYellow
  }
}