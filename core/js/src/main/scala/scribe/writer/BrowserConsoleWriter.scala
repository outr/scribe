package scribe.writer

import scribe._
import scribe.Platform._
import scribe.output._

import scala.collection.mutable.ListBuffer
import scala.scalajs.js

object BrowserConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    val b = new StringBuilder
    val args = ListBuffer.empty[String]
    recurse(b, args, None, None, output)
    val jsArgs = args.map(js.Any.fromString).toList

    if (record.level >= Level.Error) {
      console.error(b.toString(), jsArgs: _*)
    } else if (record.level >= Level.Warn) {
      console.warn(b.toString(), jsArgs: _*)
    } else {
      console.log(b.toString(), jsArgs: _*)
    }
  }

  private def recurse(b: StringBuilder,
                      args: ListBuffer[String],
                      fg: Option[String],
                      bg: Option[String],
                      output: LogOutput): Unit = output match {
    case o: TextOutput => b.append(o.plainText)
    case o: CompositeOutput => o.entries.foreach(recurse(b, args, fg, bg, _))
    case o: ColoredOutput => {
      val color = color2CSS(o.color)
      b.append("%c")
      val css = s"color: $color"
      args += css
      recurse(b, args, Some(css), bg, o.output)
      b.append("%c")
      args += fg.getOrElse(s"color: ${color2CSS(Color.Black)}")
    }
    case o: BackgroundColoredOutput => {
      val color = color2CSS(o.color)
      b.append("%c")
      val css = s"background-color: $color"
      args += css
      recurse(b, args, fg, Some(css), o.output)
      b.append("%c")
      args += bg.getOrElse(s"background-color: ${color2CSS(Color.White)}")
    }
    case o: URLOutput => {
      b.append("%o (")
      args += o.url
      recurse(b, args, fg, bg, o.output)
      b.append(")")
    }
    case _ => b.append(output.plainText)
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