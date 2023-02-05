package scribe.slack

import cats.effect.IO
import fabric.Json
import fabric.rw._
import scribe.format._
import scribe.handler.LogHandler
import scribe.{Level, Logger}
import spice.http.HttpResponse
import spice.http.client.HttpClient
import spice.http.content.Content
import spice.net.URL

class Slack(serviceHash: String, botName: String) {
  private lazy val client = HttpClient.url(URL.parse(s"https://hooks.slack.com/services/$serviceHash")).post

  def request(message: String,
              markdown: Boolean = true,
              attachments: List[Slack.Attachment] = Nil,
              emojiIcon: String = ":fire:"): IO[HttpResponse] = {
    val m = SlackMessage(
      text = message,
      username = botName,
      mrkdwn = markdown,
      icon_emoji = emojiIcon,
      attachments = attachments
    )
    val json = m.json
    val content = Content.json(json)
    client.content(content).send()
  }
}

object Slack {
  case class Attachment(title: String, text: String)

  object Attachment {
    implicit val rw: RW[Attachment] = RW.gen
  }

  def configure(serviceHash: String,
                botName: String,
                emojiIcon: String = ":fire:",
                loggerName: String = "slack",
                level: Level = Level.Error): Unit = {
    val slack = new Slack(serviceHash, botName)
    val formatter = formatter"[$threadName] $levelPaddedRight $positionAbbreviated - $messages"

    val handler = LogHandler(
      minimumLevel = Some(level),
      writer = new SlackWriter(slack, emojiIcon),
      formatter = formatter
    )
    Logger(loggerName).withHandler(handler).replace()
  }
}