package scribe.output.format

import scribe.ANSI
import scribe.output._

import scala.language.implicitConversions

object ANSIOutputFormat extends OutputFormat {
  private class State {
    var fg: Option[ANSI] = None
    var bg: Option[ANSI] = None
    var bold: Boolean = false
    var italic: Boolean = false
    var underline: Boolean = false
    var strikethrough: Boolean = false
  }

  private val threadState = new ThreadLocal[State] {
    override def initialValue(): State = new State
  }

  override def begin(stream: String => Unit): Unit = {}

  override def end(stream: String => Unit): Unit = {
    stream(ANSI.ctrl.Reset)
  }

  def apply(output: LogOutput, stream: String => Unit): Unit = {
    val state = threadState.get()

    def reset(stream: String => Unit): Unit = {
      stream(ANSI.ctrl.Reset)
      state.fg.map(_.ansi).foreach(stream)
      state.bg.map(_.ansi).foreach(stream)
      if (state.bold) stream(ANSI.fx.Bold.ansi)
      if (state.italic) stream(ANSI.fx.Italic.ansi)
      if (state.underline) stream(ANSI.fx.Underline.ansi)
      if (state.strikethrough) stream(ANSI.fx.Strikethrough.ansi)
    }

    output match {
      case o: TextOutput => stream(o.plainText)
      case o: CompositeOutput => o.entries.foreach(apply(_, stream))
      case o: ColoredOutput =>
        val color = color2fg(o.color)
        stream(color.ansi)
        val previous = state.fg
        state.fg = Some(color)
        try {
          apply(o.output, stream)
        } finally {
          state.fg = previous
          reset(stream)
        }
      case o: BackgroundColoredOutput =>
        val color = color2bg(o.color)
        stream(color.ansi)
        val previous = state.bg
        state.bg = Some(color)
        try {
          apply(o.output, stream)
        } finally {
          state.bg = previous
          reset(stream)
        }
      case o: URLOutput =>
        stream("""]8;;""")
        stream(o.url)
        stream("""\""")
        if (o.output == EmptyOutput) {
          stream(o.url)
        } else {
          apply(o.output, stream)
        }
        stream("""]8;;\""")
      case o: BoldOutput =>
        val previous = state.bold
        state.bold = true
        try {
          stream(ANSI.fx.Bold.ansi)
          apply(o.output, stream)
        } finally {
          state.bold = previous
          reset(stream)
        }
      case o: ItalicOutput =>
        val previous = state.italic
        state.italic = true
        try {
          stream(ANSI.fx.Italic.ansi)
          apply(o.output, stream)
        } finally {
          state.italic = previous
          reset(stream)
        }
      case o: UnderlineOutput =>
        val previous = state.underline
        state.underline = true
        try {
          stream(ANSI.fx.Underline.ansi)
          apply(o.output, stream)
        } finally {
          state.underline = previous
          reset(stream)
        }
      case o: StrikethroughOutput =>
        val previous = state.strikethrough
        state.strikethrough = true
        try {
          stream(ANSI.fx.Strikethrough.ansi)
          apply(o.output, stream)
        } finally {
          state.strikethrough = previous
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
