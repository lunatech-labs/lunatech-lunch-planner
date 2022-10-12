package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import play.api._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import lunatech.lunchplanner.services.UserService
import scala.concurrent.ExecutionContext.Implicits.global

class RestController @Inject() (
    userService: UserService,
    val sessionCookieBaker: DefaultSessionCookieBaker,
    val configuration: Configuration,
    override val controllerComponents: ControllerComponents
) extends InjectedController
    with ApiSecured
    with I18nSupport
    with Logging {

  /** Get user by email address
    *
    * @return
    *   An optional User
    */
  def getUser(email: String): Action[AnyContent] = apiAction.async {
    implicit request =>
      userService
        .getByEmailAddress(emailAddress = email)
        .map {
          case Some(user) => Ok(Json.toJson(user))
          case None       => NotFound("User not found")
        }
  }
}
