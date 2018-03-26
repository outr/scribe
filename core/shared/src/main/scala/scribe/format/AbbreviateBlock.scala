package scribe.format

import scribe.LogRecord
import scribe.util.Abbreviator

class AbbreviateBlock(block: FormatBlock, maxLength: Int, separator: Char) extends FormatBlock {
  override def format[M](record: LogRecord[M]): String = {
    val value = block.format(record)
    Abbreviator(value, maxLength, separator)
  }
}
