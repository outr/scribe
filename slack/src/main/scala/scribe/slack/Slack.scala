package scribe.slack

import io.youi.client.HttpClient
import io.youi.http.{Content, HttpRequest, HttpResponse, Method}
import io.youi.net.{ContentType, URL}
import profig.JsonUtil
import scribe._
import scribe.format._
import scribe.handler.LogHandler
import perfolation._

import scala.concurrent.Future

class Slack(serviceHash: String, botName: String) {
  private lazy val client = HttpClient()
  private lazy val url: URL = URL(p"https://hooks.slack.com/services/$serviceHash")

  def request(message: String,
              markdown: Boolean = true,
              attachments: List[Slack.Attachment] = Nil,
              emojiIcon: String = ":fire:"): Future[HttpResponse] = {
    val m = SlackMessage(
      text = message,
      username = botName,
      mrkdwn = markdown,
      icon_emoji = emojiIcon,
      attachments = attachments
    )
    val json = JsonUtil.toJsonString(m)
    val content = Content.string(json, ContentType.`application/json`)
    val request = HttpRequest(
      method = Method.Post,
      url = url,
      content = Some(content)
    )
    client.send(request)
  }
}

object Slack {
  case class Attachment(title: String, text: String)

  def configure(serviceHash: String,
                botName: String,
                emojiIcon: String = ":fire:",
                loggerName: String = "slack",
                level: Level = Level.Error): Unit = {
    val slack = new Slack(serviceHash, botName)
    val formatter = formatter"[$threadName] $levelPaddedRight $positionAbbreviated - $message$newLine"

    val handler = LogHandler(
      minimumLevel = Some(level),
      writer = new SlackWriter(slack, emojiIcon),
      formatter = formatter
    )
    Logger(loggerName).withHandler(handler).replace()
  }
}