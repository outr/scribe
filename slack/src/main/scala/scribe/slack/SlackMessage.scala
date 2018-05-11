package scribe.slack

case class SlackMessage(text: String,
                        username: String,
                        mrkdwn: Boolean,
                        icon_emoji: String,
                        attachments: List[Slack.Attachment])