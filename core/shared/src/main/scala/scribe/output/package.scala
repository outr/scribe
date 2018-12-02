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

  def black(output: LogOutput*): LogOutput = fg(Color.Black, output: _*)
  def blue(output: LogOutput*): LogOutput = fg(Color.Blue, output: _*)
  def cyan(output: LogOutput*): LogOutput = fg(Color.Cyan, output: _*)
  def green(output: LogOutput*): LogOutput = fg(Color.Green, output: _*)
  def magenta(output: LogOutput*): LogOutput = fg(Color.Magenta, output: _*)
  def red(output: LogOutput*): LogOutput = fg(Color.Red, output: _*)
  def white(output: LogOutput*): LogOutput = fg(Color.White, output: _*)
  def yellow(output: LogOutput*): LogOutput = fg(Color.Yellow, output: _*)
  def gray(output: LogOutput*): LogOutput = fg(Color.Gray, output: _*)
  def brightBlue(output: LogOutput*): LogOutput = fg(Color.BrightBlue, output: _*)
  def brightCyan(output: LogOutput*): LogOutput = fg(Color.BrightCyan, output: _*)
  def brightGreen(output: LogOutput*): LogOutput = fg(Color.BrightGreen, output: _*)
  def brightMagenta(output: LogOutput*): LogOutput = fg(Color.BrightMagenta, output: _*)
  def brightRed(output: LogOutput*): LogOutput = fg(Color.BrightRed, output: _*)
  def brightWhite(output: LogOutput*): LogOutput = fg(Color.BrightWhite, output: _*)
  def brightYellow(output: LogOutput*): LogOutput = fg(Color.BrightYellow, output: _*)

  def bgBlack(output: LogOutput*): LogOutput = bg(Color.Black, output: _*)
  def bgBlue(output: LogOutput*): LogOutput = bg(Color.Blue, output: _*)
  def bgCyan(output: LogOutput*): LogOutput = bg(Color.Cyan, output: _*)
  def bgGreen(output: LogOutput*): LogOutput = bg(Color.Green, output: _*)
  def bgMagenta(output: LogOutput*): LogOutput = bg(Color.Magenta, output: _*)
  def bgRed(output: LogOutput*): LogOutput = bg(Color.Red, output: _*)
  def bgWhite(output: LogOutput*): LogOutput = bg(Color.White, output: _*)
  def bgYellow(output: LogOutput*): LogOutput = bg(Color.Yellow, output: _*)
  def bgGray(output: LogOutput*): LogOutput = bg(Color.Gray, output: _*)
  def bgBrightBlue(output: LogOutput*): LogOutput = bg(Color.BrightBlue, output: _*)
  def bgBrightCyan(output: LogOutput*): LogOutput = bg(Color.BrightCyan, output: _*)
  def bgBrightGreen(output: LogOutput*): LogOutput = bg(Color.BrightGreen, output: _*)
  def bgBrightMagenta(output: LogOutput*): LogOutput = bg(Color.BrightMagenta, output: _*)
  def bgBrightRed(output: LogOutput*): LogOutput = bg(Color.BrightRed, output: _*)
  def bgBrightWhite(output: LogOutput*): LogOutput = bg(Color.BrightWhite, output: _*)
  def bgBrightYellow(output: LogOutput*): LogOutput = bg(Color.BrightYellow, output: _*)
}