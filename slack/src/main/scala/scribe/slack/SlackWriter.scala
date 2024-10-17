package scribe.slack

import scribe.LogRecord
import scribe.output.LogOutput
import scribe.output.format.OutputFormat
import scribe.writer.Writer

/**
  * SlackWriter is
  *
  * @param slack Slack instance
  * @param emojiIcon the emoji to use when sending messages
  */
class SlackWriter(slack: Slack, emojiIcon: String) extends Writer {
  import cats.effect.unsafe.implicits.global

  override def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = slack.request(
    message = output.plainText,
    emojiIcon = emojiIcon
  ).unsafeRunAndForget()
}
