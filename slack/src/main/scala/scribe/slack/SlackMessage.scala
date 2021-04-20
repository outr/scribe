package scribe.slack

import fabric.rw._

case class SlackMessage(text: String,
                        username: String,
                        mrkdwn: Boolean,
                        icon_emoji: String,
                        attachments: List[Slack.Attachment])

object SlackMessage {
  implicit val rw: ReaderWriter[SlackMessage] = ccRW
}