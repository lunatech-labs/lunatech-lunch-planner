package lunatech.lunchplanner.controllers

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import play.api._
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import lunatech.lunchplanner.services.{
  DishService,
  MenuDishService,
  MenuPerDayService,
  MenuService,
  RestService,
  UserService
}
import com.lunatech.openconnect.{APISessionCookieBaker, GoogleApiSecured}
import lunatech.lunchplanner.models.{Event, MenuPerDay, MenuWithDishes, User}

class RestController @Inject() (
    userService: UserService,
    restService: RestService,
    val apiSessionCookieBaker: APISessionCookieBaker,
    val configuration: Configuration,
    override val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends InjectedController
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

  /** Get future events, including current user attendance
    *
    * @param limit
    *   Up to how many months in the future to look into
    * @return
    *   A list of Events, including user attendance and dishes
    */
  def getFutureEvents(limit: Int): Action[AnyContent] = userAction.async {
    implicit request =>
      userService.getByEmailAddress(emailAddress = request.email).flatMap {
        case Some(user: User) =>
          restService.getFutureEvents(user, limit).map { events: Seq[Event] =>
            Ok(Json.toJson(events))
          }
        case None => Future.successful(NotFound("User not found"))
      }
  }
}
