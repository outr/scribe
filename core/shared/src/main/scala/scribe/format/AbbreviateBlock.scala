package scribe.format

import scribe.LogRecord
import scribe.output.{LogOutput, TextOutput}
import scribe.util.Abbreviator

class AbbreviateBlock(block: FormatBlock,
                      maxLength: Int,
                      separator: Char,
                      removeEntries: Boolean,
                      abbreviateName: Boolean) extends FormatBlock {
  private lazy val cache = new ThreadLocal[Map[String, TextOutput]] {
    override def initialValue(): Map[String, TextOutput] = Map.empty
  }

  override def format[M](record: LogRecord[M]): LogOutput = {
    val value = block.format(record).plainText
    val map = cache.get()
    map.get(value) match {
      case Some(output) => output
      case None =>
        val abbreviated = Abbreviator(value, maxLength, separator, removeEntries, abbreviateName)
        val output = new TextOutput(abbreviated)
        cache.set(map + (value -> output))
        output
    }
  }
}