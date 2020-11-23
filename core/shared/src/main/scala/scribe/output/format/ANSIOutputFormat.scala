package scribe.output.format

import scribe.output._
import scribe.ANSI

import scala.language.implicitConversions

object ANSIOutputFormat extends OutputFormat {
  private object ansi {
    var fg: Option[ANSI] = None
    var bg: Option[ANSI] = None
    var bold: Boolean = false
    var italic: Boolean = false
    var underline: Boolean = false
    var strikethrough: Boolean = false
  }

  def apply(output: LogOutput, stream: String => Unit): Unit = output match {
    case o: TextOutput => stream(o.plainText)
    case o: CompositeOutput => o.entries.foreach(apply(_, stream))
    case o: ColoredOutput => {
      val color = color2fg(o.color)
      stream(color.ansi)
      val previous = ansi.fg
      ansi.fg = Some(color)
      try {
        apply(o.output, stream)
      } finally {
        ansi.fg = previous
        reset(stream)
      }
    }
    case o: BackgroundColoredOutput => {
      val color = color2bg(o.color)
      stream(color.ansi)
      val previous = ansi.bg
      ansi.bg = Some(color)
      try {
        apply(o.output, stream)
      } finally {
        ansi.bg = previous
        reset(stream)
      }
    }
    case o: URLOutput => {
      stream("""]8;;""")
      stream(o.url)
      stream("""\""")
      if (o.output == EmptyOutput) {
        stream(o.url)
      } else {
        apply(o.output, stream)
      }
      stream("""]8;;\""")
    }
    case o: BoldOutput => {
      val previous = ansi.bold
      ansi.bold = true
      try {
        stream(ANSI.fx.Bold.ansi)
        apply(o.output, stream)
      } finally {
        ansi.bold = previous
        reset(stream)
      }
    }
    case o: ItalicOutput => {
      val previous = ansi.italic
      ansi.italic = true
      try {
        stream(ANSI.fx.Italic.ansi)
        apply(o.output, stream)
      } finally {
        ansi.italic = previous
        reset(stream)
      }
    }
    case o: UnderlineOutput => {
      val previous = ansi.underline
      ansi.underline = true
      try {
        stream(ANSI.fx.Underline.ansi)
        apply(o.output, stream)
      } finally {
        ansi.underline = previous
        reset(stream)
      }
    }
    case o: StrikethroughOutput => {
      val previous = ansi.strikethrough
      ansi.strikethrough = true
      try {
        stream(ANSI.fx.Strikethrough.ansi)
        apply(o.output, stream)
      } finally {
        ansi.strikethrough = previous
        reset(stream)
      }
    }
    case _ => stream(output.plainText)      // TODO: support warning unsupported
  }

  private def reset(stream: String => Unit): Unit = {
    stream(ANSI.ctrl.Reset)
    ansi.fg.map(_.ansi).foreach(stream)
    ansi.bg.map(_.ansi).foreach(stream)
    if (ansi.bold) stream(ANSI.fx.Bold.ansi)
    if (ansi.italic) stream(ANSI.fx.Italic.ansi)
    if (ansi.underline) stream(ANSI.fx.Underline.ansi)
    if (ansi.strikethrough) stream(ANSI.fx.Strikethrough.ansi)
  }

  private def color2fg(color: Color): ANSI = color match {
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

  private def color2bg(color: Color): ANSI = color match {
    case Color.Black => ANSI.bg.Black
    case Color.Blue => ANSI.bg.Blue
    case Color.Cyan => ANSI.bg.Cyan
    case Color.Green => ANSI.bg.Green
    case Color.Magenta => ANSI.bg.Magenta
    case Color.Red => ANSI.bg.Red
    case Color.White => ANSI.bg.White
    case Color.Yellow => ANSI.bg.Yellow
    case Color.Gray => ANSI.bg.Gray
    case Color.BrightBlue => ANSI.bg.BrightBlue
    case Color.BrightCyan => ANSI.bg.BrightCyan
    case Color.BrightGreen => ANSI.bg.BrightGreen
    case Color.BrightMagenta => ANSI.bg.BrightMagenta
    case Color.BrightRed => ANSI.bg.BrightRed
    case Color.BrightWhite => ANSI.bg.BrightWhite
    case Color.BrightYellow => ANSI.bg.BrightYellow
  }
}