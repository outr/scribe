package scribe.format

import scribe.LogRecord
import scribe.output.{CompositeOutput, LogOutput}

class FormatBlocksFormatter(blocks: List[FormatBlock]) extends Formatter {
  override def format(record: LogRecord): LogOutput = {
    new CompositeOutput(blocks.map(_.format(record)))
  }

  override def toString: String = s"blocks(${blocks.mkString(", ")})"
}