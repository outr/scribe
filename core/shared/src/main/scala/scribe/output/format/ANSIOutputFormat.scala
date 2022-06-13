package scribe.output.format

import scribe.ANSI
import scribe.output._

import scala.language.implicitConversions

object ANSIOutputFormat extends OutputFormat {
  private var fg: Option[ANSI] = None
  private var bg: Option[ANSI] = None
  private var bold: Boolean = false
  private var italic: Boolean = false
  private var underline: Boolean = false
  private var strikethrough: Boolean = false

  override def begin(stream: String => Unit): Unit = {}

  override def end(stream: String => Unit): Unit = {
    stream(ANSI.ctrl.Reset)
  }

  def apply(output: LogOutput, stream: String => Unit): Unit = synchronized {
    def reset(stream: String => Unit): Unit = {
      stream(ANSI.ctrl.Reset)
      fg.map(_.ansi).foreach(stream)
      bg.map(_.ansi).foreach(stream)
      if (bold) stream(ANSI.fx.Bold.ansi)
      if (italic) stream(ANSI.fx.Italic.ansi)
      if (underline) stream(ANSI.fx.Underline.ansi)
      if (strikethrough) stream(ANSI.fx.Strikethrough.ansi)
    }

    output match {
      case o: TextOutput => stream(o.plainText)
      case o: CompositeOutput => o.entries.foreach(apply(_, stream))
      case o: ColoredOutput =>
        val color = color2fg(o.color)
        stream(color.ansi)
        val previous = fg
        fg = Some(color)
        try {
          apply(o.output, stream)
        } finally {
          fg = previous
          reset(stream)
        }
      case o: BackgroundColoredOutput =>
        val color = color2bg(o.color)
        stream(color.ansi)
        val previous = bg
        bg = Some(color)
        try {
          apply(o.output, stream)
        } finally {
          bg = previous
          reset(stream)
        }
      case o: URLOutput =>
        stream("""]8;;""")
        stream(o.url)
        stream("""\""")
        if (o.output == EmptyOutput) {
          stream(o.url)
        } else {
          apply(o.output, stream)
        }
        stream("""]8;;\""")
      case o: BoldOutput =>
        val previous = bold
        bold = true
        try {
          stream(ANSI.fx.Bold.ansi)
          apply(o.output, stream)
        } finally {
          bold = previous
          reset(stream)
        }
      case o: ItalicOutput =>
        val previous = italic
        italic = true
        try {
          stream(ANSI.fx.Italic.ansi)
          apply(o.output, stream)
        } finally {
          italic = previous
          reset(stream)
        }
      case o: UnderlineOutput =>
        val previous = underline
        underline = true
        try {
          stream(ANSI.fx.Underline.ansi)
          apply(o.output, stream)
        } finally {
          underline = previous
          reset(stream)
        }
      case o: StrikethroughOutput =>
        val previous = strikethrough
        strikethrough = true
        try {
          stream(ANSI.fx.Strikethrough.ansi)
          apply(o.output, stream)
        } finally {
          strikethrough = previous
          reset(stream)
        }
      case EmptyOutput => // Nothing to do
    }
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