package lunatech.lunchplanner.controllers

import javax.inject.Inject

import lunatech.lunchplanner.services.SlackService
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, BaseController, ControllerComponents, EssentialAction }

import scala.concurrent.ExecutionContext.Implicits.global

class SlackController @Inject()(
  val controllerComponents: ControllerComponents,
  val slackService: SlackService,
  val configuration: Configuration) extends BaseController {

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
