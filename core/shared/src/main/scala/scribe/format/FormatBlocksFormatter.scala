package scribe.format

import scribe.LogRecord

class FormatBlocksFormatter(blocks: List[FormatBlock]) extends Formatter {
  override def format(record: LogRecord): String = {
    val b = new java.lang.StringBuilder
    blocks.foreach(_.format(record, b))
    b.toString
  }
}
