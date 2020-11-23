package scribe.output.format
import scribe.output.{BackgroundColoredOutput, BoldOutput, Color, ColoredOutput, CompositeOutput, ItalicOutput, LogOutput, StrikethroughOutput, TextOutput, URLOutput, UnderlineOutput}
import scribe.writer.BrowserConsoleWriter

import scala.collection.mutable.ListBuffer

object RichBrowserOutputFormat extends OutputFormat {
  override def apply(output: LogOutput, stream: String => Unit): Unit = recurse(
    stream = stream,
    args = BrowserConsoleWriter.args,
    fg = None,
    bg = None,
    bold = false,
    italic = false,
    underline = false,
    strikethrough = false,
    output = output
  )

  private def recurse(stream: String => Unit,
                      args: ListBuffer[String],
                      fg: Option[String],
                      bg: Option[String],
                      bold: Boolean,
                      italic: Boolean,
                      underline: Boolean,
                      strikethrough: Boolean,
                      output: LogOutput): Unit = output match {
    case o: TextOutput => stream(o.plainText)
    case o: CompositeOutput => o.entries.foreach(recurse(stream, args, fg, bg, bold, italic, underline, strikethrough, _))
    case o: ColoredOutput => {
      val color = color2CSS(o.color)
      stream("%c")
      val css = s"color: $color"
      args += css
      recurse(stream, args, Some(css), bg, bold, italic, underline, strikethrough, o.output)
      stream("%c")
      args += fg.getOrElse(s"color: ${color2CSS(Color.Black)}")
    }
    case o: BackgroundColoredOutput => {
      val color = color2CSS(o.color)
      stream("%c")
      val css = s"background-color: $color"
      args += css
      recurse(stream, args, fg, Some(css), bold, italic, underline, strikethrough, o.output)
      stream("%c")
      args += bg.getOrElse(s"background-color: ${color2CSS(Color.White)}")
    }
    case o: URLOutput => {
      stream("%o (")
      args += o.url
      recurse(stream, args, fg, bg, bold, italic, underline, strikethrough, o.output)
      stream(")")
    }
    case o: BoldOutput => if (!bold) {
      stream("%c")
      val css = "font-weight: bold"
      args += css
      recurse(stream, args, fg, bg, true, italic, underline, strikethrough, o.output)
      stream("%c")
      args += "font-weight: normal"
    }
    case o: ItalicOutput => if (!italic) {
      stream("%c")
      val css = "font-style: italic"
      args += css
      recurse(stream, args, fg, bg, bold, true, underline, strikethrough, o.output)
      stream("%c")
      args += "font-style: normal"
    }
    case o: UnderlineOutput => if (!underline) {
      stream("%c")
      val css = "text-decoration: underline"
      args += css
      recurse(stream, args, fg, bg, bold, italic, true, strikethrough, o.output)
      stream("%c")
      args += "text-decoration: none"
    }
    case o: StrikethroughOutput => if (!strikethrough) {
      stream("%c")
      val css = "text-decoration: line-through"
      args += css
      recurse(stream, args, fg, bg, bold, italic, underline, true, o.output)
      stream("%c")
      args += "text-decoration: none"
    }
    case _ => stream(output.plainText)
  }

  private def color2CSS(color: Color): String = color match {
    case Color.Black => "black"
    case Color.Blue => "blue"
    case Color.Cyan => "cyan"
    case Color.Green => "green"
    case Color.Magenta => "magenta"
    case Color.Red => "red"
    case Color.White => "white"
    case Color.Yellow => "yellow"
    case Color.Gray => "gray"
    case Color.BrightBlue => "lightblue"
    case Color.BrightCyan => "lightcyan"
    case Color.BrightGreen => "lime"
    case Color.BrightMagenta => "violet"
    case Color.BrightRed => "crimson"
    case Color.BrightWhite => "white"
    case Color.BrightYellow => "lightyellow"
  }
}