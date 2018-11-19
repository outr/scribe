package scribe.output

trait LogOutput extends Any {
  def plainText: String
}

class TextOutput(val plainText: String) extends AnyVal with LogOutput

class CompositeOutput(val entries: List[LogOutput]) extends LogOutput {
  override lazy val plainText: String = entries.map(_.plainText).mkString
}

class ColoredOutput(color: Color, output: LogOutput) extends LogOutput {
  override lazy val plainText: String = output.plainText
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
}