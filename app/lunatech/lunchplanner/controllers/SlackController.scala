package lunatech.lunchplanner.controllers

import lunatech.lunchplanner.services.SlackService
import lunatech.lunchplanner.viewModels.SlackForm
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SlackController @Inject() (
    val controllerComponents: ControllerComponents,
    val slackService: SlackService,
    val configuration: Configuration
) extends BaseController {

  def processSlackRequest: EssentialAction = Action.async { implicit request =>
    val req = request.body.asFormUrlEncoded.getOrElse(Map())
    req("payload").headOption match {
      case Some(json) =>
        val slackResponse =
          SlackForm.jsonToSlackResponseObject(Json.parse(json))

        slackService
          .processSlackRequest(slackResponse)
          .flatMap(_ => slackService.processSlackResponse(slackResponse))
          .map(Ok(_))

      case None => Future.successful(BadRequest("Payload not available"))
    }
  }
}
