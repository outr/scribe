package scribe.slack

import gigahorse.{FullResponse, MimeTypes}
import gigahorse.support.asynchttpclient.Gigahorse
import scribe._
import scribe.format._
import scribe.modify.LevelFilter
import upickle.Js

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Slack(serviceHash: String, botName: String) {
  def request(message: String,
              markdown: Boolean = true,
              attachment: Option[Slack.Attachment] = None,
              emojiIcon: String = ":fire:"): Future[FullResponse] = {
    val http = Gigahorse.http(Gigahorse.config)
    val json = upickle.json.write(Js.Obj(
      Seq(
        "text" -> Js.Str(message),
        "username" -> Js.Str(botName),
        "mrkdwn" -> (if (markdown) Js.True else Js.False),
        "icon_emoji" -> Js.Str(emojiIcon)
      ) ++ (
        attachment match {
          case None => Seq.empty
          case Some(s) => Seq("attachments" -> Js.Arr(
            Js.Obj(
              "title" -> Js.Str(s.title),
              "fallback" -> Js.Str(s.title),
              "text" -> Js.Str(s.text)
            )
          ))
        }
        ): _*
    ))

    val r = Gigahorse
      .url(s"https://hooks.slack.com/services/$serviceHash")
      .post(json)
      .withContentType(MimeTypes.JSON, Gigahorse.utf8)
      .withRequestTimeout(5.seconds)

    val future = http.run(r)
    future.onComplete { t =>
      http.close()
    }
    future
  }
}

object Slack {
  case class Attachment(title: String, text: String)

  def configure(serviceHash: String,
                botName: String,
                emojiIcon: String = ":fire:",
                loggerName: String = Logger.rootName,
                level: Level = Level.Error): Unit = {
    val slack = new Slack(serviceHash, botName)
    val formatter = formatter"[$threadName] $levelPaddedRight $positionAbbreviated - $message$newLine"

    val handler = LogHandler
      .default
      .withModifier(LevelFilter >= level)
      .withWriter(new SlackWriter(slack, emojiIcon))
      .withFormatter(formatter)
    Logger.update(loggerName)(_.withHandler(handler))
  }
}