package scribe

import scala.language.experimental.macros

package object format {
  def date: FormatBlock = FormatBlock.Date.Standard
  def threadName: FormatBlock = FormatBlock.ThreadName
  def levelPaddedRight: FormatBlock = FormatBlock.Level.PaddedRight
  def position: FormatBlock = FormatBlock.Position.Full
  def positionAbbreviated: FormatBlock = FormatBlock.Position.Abbreviated
  def message: FormatBlock = FormatBlock.Message
  def newLine: FormatBlock = FormatBlock.NewLine

  implicit class FormatterInterpolator(val sc: StringContext) extends AnyVal {
    def formatter(args: Any*): Formatter = macro Macros.formatter
  }
}
