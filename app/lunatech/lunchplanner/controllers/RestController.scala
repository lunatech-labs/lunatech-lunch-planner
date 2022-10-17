package lunatech.lunchplanner.controllers

import scala.concurrent.ExecutionContext

import com.google.inject.Inject
import play.api._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import lunatech.lunchplanner.services.UserService
import com.lunatech.openconnect.{APISessionCookieBaker, GoogleApiSecured}

class RestController @Inject() (
    userService: UserService,
    val apiSessionCookieBaker: APISessionCookieBaker,
    val configuration: Configuration,
    override val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends InjectedController
    with GoogleApiSecured
    with I18nSupport
    with Logging {

  /** Get user by email address
    *
    * @return
    *   An optional User
    */
  def getUser(email: String): Action[AnyContent] = userAction.async {
    implicit request =>
      userService
        .getByEmailAddress(emailAddress = email)
        .map {
          case Some(user) => Ok(Json.toJson(user))
          case None       => NotFound("User not found")
        }
  }
}
