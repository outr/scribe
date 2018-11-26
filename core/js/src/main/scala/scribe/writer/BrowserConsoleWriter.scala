package scribe.writer

import scribe._
import scribe.Platform._
import scribe.output._

import scala.collection.mutable.ListBuffer
import scala.scalajs.js

object BrowserConsoleWriter extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = {
    val b = new StringBuilder
    val fgColors = ListBuffer.empty[String]
    recurse(b, fgColors, None, output)
    val args = fgColors.map(c => js.Any.fromString(s"color: $c")).toList

    if (record.level >= Level.Error) {
      console.error(b.toString(), args: _*)
    } else if (record.level >= Level.Warn) {
      console.warn(b.toString(), args: _*)
    } else {
      console.log(b.toString(), args: _*)
    }
  }

  private def recurse(b: StringBuilder,
                      fgColors: ListBuffer[String],
                      fg: Option[String],
                      output: LogOutput): Unit = output match {
    case o: TextOutput => b.append(o.plainText)
    case o: CompositeOutput => o.entries.foreach(recurse(b, fgColors, fg, _))
    case o: ColoredOutput => {
      val color = color2CSS(o.color)
      b.append("%c")
      fgColors += color
      recurse(b, fgColors, Some(color), o.output)
      b.append("%c")
      fgColors += fg.getOrElse(color2CSS(Color.Black))
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