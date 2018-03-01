package scribe.format

import scribe.LogRecord

import scala.annotation.tailrec

class FormatBlocksFormatter(blocks: List[FormatBlock]) extends Formatter {
  override def format(record: LogRecord): String = {
    val b = new java.lang.StringBuilder
    applyBlocks(b, record, blocks)
    b.toString
  }

  @tailrec
  private def applyBlocks(b: java.lang.StringBuilder,
                          record: LogRecord,
                          blocks: List[FormatBlock]): String = if (blocks.isEmpty) {
    b.toString
  } else {
    b.append(blocks.head.format(record))
    applyBlocks(b, record, blocks.tail)
  }
}