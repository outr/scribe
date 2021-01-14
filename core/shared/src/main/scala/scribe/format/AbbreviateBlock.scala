package scribe.format

import scribe.LogRecord
import scribe.output.{LogOutput, TextOutput}
import scribe.util.Abbreviator

class AbbreviateBlock(block: FormatBlock,
                      maxLength: Int,
                      separator: Char,
                      removeEntries: Boolean,
                      abbreviateName: Boolean) extends FormatBlock {
  override def format[M](record: LogRecord[M]): LogOutput = {
    val value = block.format(record).plainText
    new TextOutput(Abbreviator(value, maxLength, separator, removeEntries, abbreviateName))
  }
}