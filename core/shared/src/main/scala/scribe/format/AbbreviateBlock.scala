package scribe.format

import scribe.LogRecord
import scribe.util.Abbreviator

class AbbreviateBlock(block: FormatBlock,
                      maxLength: Int,
                      separator: Char,
                      removeEntries: Boolean,
                      abbreviateName: Boolean) extends FormatBlock {
  override def format[M](record: LogRecord[M]): String = {
    val value = block.format(record)
    Abbreviator(value, maxLength, separator, removeEntries, abbreviateName)
  }
}
