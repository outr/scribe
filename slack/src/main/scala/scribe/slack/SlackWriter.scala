package scribe.slack

import scribe.LogRecord
import scribe.output.LogOutput
import scribe.writer.Writer

/**
  * SlackWriter is
  *
  * @param slack Slack instance
  * @param emojiIcon the emoji to use when sending messages
  */
class SlackWriter(slack: Slack, emojiIcon: String) extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput): Unit = slack.request(
    message = output.plainText,
    emojiIcon = emojiIcon
  )
}