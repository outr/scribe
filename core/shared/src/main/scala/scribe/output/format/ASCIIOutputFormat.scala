package scribe.output.format
import scribe.output.LogOutput

object ASCIIOutputFormat extends OutputFormat {
  override def apply(output: LogOutput, stream: String => Unit): Unit = stream(output.plainText)
}
