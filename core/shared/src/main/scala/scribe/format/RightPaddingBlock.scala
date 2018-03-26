package scribe.format

import scribe.LogRecord

class RightPaddingBlock(block: FormatBlock, length: Int, padding: Char) extends FormatBlock {
  override def format[M](record: LogRecord[M]): String = {
    val value = block.format(record)
    value.padTo(length, " ").mkString
  }
}
