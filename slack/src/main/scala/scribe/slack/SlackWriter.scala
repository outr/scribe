package scribe.slack

import scribe.LogRecord
import scribe.formatter.Formatter
import scribe.writer.Writer

/**
  * SlackWriter is
  *
  * @param slack
  * @param emojiIcon
  */
class SlackWriter(slack: Slack, emojiIcon: String) extends Writer {
  def write(record: LogRecord, formatter: Formatter): Unit = {
    slack.request(formatter.format(record), emojiIcon = emojiIcon)
  }
}