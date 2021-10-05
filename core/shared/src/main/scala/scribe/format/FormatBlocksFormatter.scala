package scribe.format

import scribe.LogRecord
import scribe.output.{CompositeOutput, LogOutput}

import scala.annotation.tailrec

class FormatBlocksFormatter(blocks: List[FormatBlock]) extends Formatter {
  override def format[M](record: LogRecord[M]): LogOutput = {
    new CompositeOutput(blocks.map(_.format(record)))
  }
}