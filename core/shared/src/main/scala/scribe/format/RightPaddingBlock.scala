package scribe.format

import scribe.LogRecord
import scribe.output.{LogOutput, TextOutput}

class RightPaddingBlock(block: FormatBlock, length: Int, padding: Char) extends FormatBlock {
  override def format[M](record: LogRecord[M]): LogOutput = {
    val value = block.format(record).plainText
    new TextOutput(value.padTo(length, " ").mkString)
  }
}
