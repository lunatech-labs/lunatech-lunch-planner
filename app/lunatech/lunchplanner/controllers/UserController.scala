package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.services.UserService
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, EssentialAction}

import scala.concurrent.ExecutionContext.Implicits.global

class UserController @Inject()(userService: UserService) extends Controller {

  def getAllEmailAddresses(): EssentialAction = Action.async {
    implicit request => {
      for {
        emails <- userService.getAllEmailAddresses()
      } yield Ok(Json.toJson(emails))
    }
  }
}
