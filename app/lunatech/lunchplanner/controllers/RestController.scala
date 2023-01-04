package lunatech.lunchplanner.controllers

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.lunatech.openconnect.{
  APISessionCookieBaker,
  Authenticate,
  GoogleApiSecured
}
import lunatech.lunchplanner.models.{Event, User}
import lunatech.lunchplanner.services.{RestService, UserService}
import play.api._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import lunatech.lunchplanner.models.MenuPerDayPerPerson
import java.util.UUID
import lunatech.lunchplanner.services.MenuPerDayPerPersonService

class RestController @Inject() (
    userService: UserService,
    restService: RestService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    environment: Environment,
    authenticate: Authenticate,
    val apiSessionCookieBaker: APISessionCookieBaker,
    val configuration: Configuration,
    override val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends InjectedController
    with GoogleApiSecured
    with I18nSupport
    with Logging {

  /** Authentication route for API-based requests.
    *
    * @param accessToken
    *   valid access token obtained by external parties
    * @return
    *   HTTP response with an encoded map of session properties
    */
  def validateAccessToken(accessToken: String): Action[AnyContent] =
    Action.async { implicit request =>
      if (environment.mode == Mode.Prod) {
        authenticate.getUserFromToken(accessToken).map {
          case Left(authResult) =>
            val email = authResult.email
            val data =
              Map("email" -> email, "isAdmin" -> isAdmin(email).toString)

            Ok(apiSessionCookieBaker.jwtCodec.encode(data))
          case Right(message) =>
            BadRequest(
              s"Authentication failed, reason: $message"
            ).withNewSession
        }
      } else {
        val email = "developer@lunatech.nl"
        val data  = Map("email" -> email, "isAdmin" -> isAdmin(email).toString)
        Future(Ok(apiSessionCookieBaker.jwtCodec.encode(data)))
      }
    }

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

  /** Register current user as attending a specific event (MenuPerDay)
    *
    * @param uuid
    *   MenuPerDay UUID
    * @return
    *   added / updated MenuPerDayPerPerson
    */
  def attend(uuid: UUID): Action[AnyContent] = userAction.async {
    implicit request =>
      userService.getByEmailAddress(emailAddress = request.email).flatMap {
        case Some(user: User) =>
          restService.registerAttendance(uuid, user.uuid, true).map {
            menuPerDayPerPerson =>
              Ok(Json.toJson(menuPerDayPerPerson))
          }
        case None => Future.successful(NotFound("User not found"))
      }
  }

  /** Register current user as not attending a specific event (MenuPerDay)
    *
    * @param uuid
    * @return
    *   added / updated MenuPerDayPerPerson
    */
  def unattend(uuid: UUID): Action[AnyContent] = userAction.async {
    implicit request =>
      userService.getByEmailAddress(emailAddress = request.email).flatMap {
        case Some(user: User) =>
          restService.registerAttendance(uuid, user.uuid, false).map {
            menuPerDayPerPerson =>
              Ok(Json.toJson(menuPerDayPerPerson))
          }
        case None => Future.successful(NotFound("User not found"))
      }
  }

}
