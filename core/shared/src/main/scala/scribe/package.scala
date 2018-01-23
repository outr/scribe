import scala.language.experimental.macros
import scala.language.implicitConversions

package object scribe2 {
  def date: FormatBlock = FormatBlock.Date.Standard
  def threadName: FormatBlock = FormatBlock.ThreadName
  def levelPaddedRight: FormatBlock = FormatBlock.Level.PaddedRight
  def positionAbbreviated: FormatBlock = FormatBlock.Position.Abbreviated
  def message: FormatBlock = FormatBlock.Message
  def newLine: FormatBlock = FormatBlock.NewLine

  implicit class FormatterInterpolator(val sc: StringContext) extends AnyVal {
    def formatter(args: Any*): Formatter = macro Macros.formatter
  }

  def dispose(): Unit = AsynchronousSequentialWriter.dispose()
}