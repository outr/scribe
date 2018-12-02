package scribe.output

trait LogOutput extends Any {
  def plainText: String
}

object EmptyOutput extends LogOutput {
  override val plainText: String = ""
}

class TextOutput(val plainText: String) extends AnyVal with LogOutput

class CompositeOutput(val entries: List[LogOutput]) extends LogOutput {
  override lazy val plainText: String = entries.map(_.plainText).mkString
}

class ColoredOutput(val color: Color, val output: LogOutput) extends LogOutput {
  override lazy val plainText: String = output.plainText
}

class BackgroundColoredOutput(val color: Color, val output: LogOutput) extends LogOutput {
  override lazy val plainText: String = output.plainText
}

class URLOutput(val url: String, val output: LogOutput) extends LogOutput {
  override def plainText: String = output.plainText
}

class BoldOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText
}

class ItalicOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText
}

class UnderlineOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText
}

class StrikethroughOutput(val output: LogOutput) extends AnyVal with LogOutput {
  override def plainText: String = output.plainText
}

sealed trait Color

object Color {
  case object Black extends Color
  case object Blue extends Color
  case object Cyan extends Color
  case object Green extends Color
  case object Magenta extends Color
  case object Red extends Color
  case object White extends Color
  case object Yellow extends Color

  case object Gray extends Color
  case object BrightBlue extends Color
  case object BrightCyan extends Color
  case object BrightGreen extends Color
  case object BrightMagenta extends Color
  case object BrightRed extends Color
  case object BrightWhite extends Color
  case object BrightYellow extends Color
}