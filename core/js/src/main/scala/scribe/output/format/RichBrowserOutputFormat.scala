package scribe.output.format

import scribe.output._
import scribe.writer.BrowserConsoleWriter

/**
  * Supports rich output to JavaScript console in the browser
  */
object RichBrowserOutputFormat extends OutputFormat {
  import BrowserConsoleWriter.args

  override def apply(output: LogOutput, stream: String => Unit): Unit = recurse(output, stream)

  private def recurse(output: LogOutput, stream: String => Unit): Unit = {
    def withArg(key: String, value: String, output: LogOutput): Unit = {
      stream("%c")
      args.around(key -> value) {
        recurse(output, stream)
      }
      stream("%c")
    }
    output match {
      case o: TextOutput => stream(o.plainText)
      case o: CompositeOutput => o.entries.foreach(recurse(_, stream))
      case o: ColoredOutput => withArg("color", color2CSS(o.color), o.output)
      case o: BackgroundColoredOutput => withArg("background-color", color2CSS(o.color), o.output)
      case o: URLOutput =>
        stream("%o (")
        args.around("::URL" -> o.url) {
          recurse(o.output, stream)
        }
        stream(")")
      case o: BoldOutput => withArg("font-weight", "bold", o.output)
      case o: ItalicOutput => withArg("font-style", "italic", o.output)
      case o: UnderlineOutput => withArg("text-decoration", "underline", o.output)
      case o: StrikethroughOutput => withArg("text-decoration", "line-through", o.output)
      case _ => stream(output.plainText)
    }
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