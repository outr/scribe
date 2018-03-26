package scribe

import scala.language.experimental.macros

package object format {
  def date: FormatBlock = FormatBlock.Date.Standard
  def threadName: FormatBlock = FormatBlock.ThreadName
  def level: FormatBlock = FormatBlock.Level
  def levelPaddedRight: FormatBlock = FormatBlock.Level.PaddedRight
  def position: FormatBlock = FormatBlock.Position
  def message: FormatBlock = FormatBlock.Message
  def newLine: FormatBlock = FormatBlock.NewLine

  implicit class FormatterInterpolator(val sc: StringContext) extends AnyVal {
    def formatter(args: Any*): Formatter = macro Macros.formatter
  }
}