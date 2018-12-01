package scribe

import scala.language.implicitConversions

package object output {
  implicit def text(s: String): LogOutput = new TextOutput(s)
  implicit def seq2LogOutput(entries: Seq[LogOutput]): LogOutput = new CompositeOutput(entries.toList)

  implicit class EnhancedColor(color: Color) {
    def apply(out: LogOutput*): LogOutput = fg(out: _*)
    def fg(out: LogOutput*): LogOutput = output.fg(color, out: _*)
    def bg(out: LogOutput*): LogOutput = output.bg(color, out: _*)
  }

  def out(entries: LogOutput*): LogOutput = new CompositeOutput(entries.toList)

  def color(color: Color, output: LogOutput*): LogOutput = fg(color, output: _*)

  def fg(color: Color, output: LogOutput*): LogOutput = if (output.length == 1) {
    new ColoredOutput(color, output.head)
  } else {
    new ColoredOutput(color, out(output: _*))
  }

  def bg(color: Color, output: LogOutput*): LogOutput = if (output.length == 1) {
    new BackgroundColoredOutput(color, output.head)
  } else {
    new BackgroundColoredOutput(color, out(output: _*))
  }
}