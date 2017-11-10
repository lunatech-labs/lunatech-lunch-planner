package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.services.SlackService
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, EssentialAction}

import scala.concurrent.ExecutionContext.Implicits.global

class SlackController @Inject()(val slackService: SlackService,
                                val configuration: Configuration)
  extends Controller {

  def getSlackUserIds: EssentialAction = Action.async {
    implicit request => {
      val emails = request.body.asJson.map(_.as[Seq[String]]).getOrElse(Seq())
      for {
          users <- slackService.getAllSlackUsersByEmails(emails)
      } yield Ok(Json.toJson(users))

    }
  }

  def openConversation: EssentialAction = Action.async {
    implicit request => {
      val slackUserIds = request.body.asJson.map(_.as[Seq[String]]).getOrElse(Seq())
      for {
        channelIds <- slackService.openConversation(slackUserIds)
      } yield Ok(Json.toJson(channelIds))
    }
  }

  def postMessages: EssentialAction = Action.async {
    implicit request => {
      val channelIds = request.body.asJson.map(_.as[Seq[String]]).getOrElse(Seq())
      slackService.postMessage(channelIds).map { i =>
        val messageResponse = if (i == -1) {
          "No message sent because there's no upcoming lunch this coming Friday"
        } else {
          s"Message sent to $i people!"
        }
        Ok(Json.toJson(messageResponse))
      }
    }
  }

  def processSlackRequest: EssentialAction = Action.async {
    implicit request => {
      val req = request.body.asFormUrlEncoded.getOrElse(Map())
      val json = req("payload").head
      for {
        response <- slackService.processSlackRequest(Json.parse(json))
      } yield {
        Ok(response)
      }
    }
  }
}
