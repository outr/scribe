package scribe.format

import scribe.LogRecord
import scribe.output.{CompositeOutput, LogOutput}

import scala.annotation.tailrec

class FormatBlocksFormatter(blocks: List[FormatBlock]) extends Formatter {
  override def format[M](record: LogRecord[M]): LogOutput = {
    new CompositeOutput(blocks.map(_.format(record)))
  }

  @tailrec
  private def applyBlocks[M](b: java.lang.StringBuilder,
                             record: LogRecord[M],
                             blocks: List[FormatBlock]): String = if (blocks.isEmpty) {
    b.toString
  } else {
    b.append(blocks.head.format(record))
    applyBlocks(b, record, blocks.tail)
  }
}