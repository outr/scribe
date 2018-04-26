package scribe

import scala.language.experimental.macros

package object format {
  private val ThreadNameAbbreviationLength = 10
  private val PositionAbbreviationLength = 25

  def date: FormatBlock = FormatBlock.Date.Standard
  def threadName: FormatBlock = FormatBlock.ThreadName
  def threadNameAbbreviated: FormatBlock = threadName.abbreviate(
    maxLength = ThreadNameAbbreviationLength,
    padded = true,
    abbreviateName = true
  )
  def level: FormatBlock = FormatBlock.Level
  def levelPaddedRight: FormatBlock = FormatBlock.Level.PaddedRight
  def fileName: FormatBlock = FormatBlock.FileName
  def lineNumber: FormatBlock = FormatBlock.LineNumber
  def position: FormatBlock = FormatBlock.Position
  def positionAbbreviated: FormatBlock = position.abbreviate(
    maxLength = PositionAbbreviationLength,
    padded = true,
    abbreviateName = true
  )
  def message: FormatBlock = FormatBlock.Message
  def newLine: FormatBlock = FormatBlock.NewLine
  def mdc(key: String): FormatBlock = FormatBlock.MDCReference(key)

  implicit class FormatterInterpolator(val sc: StringContext) extends AnyVal {
    def formatter(args: Any*): Formatter = macro FormatterMacros.formatter
  }
}
