package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import com.lunatech.openconnect.Authenticate
import lunatech.lunchplanner.services.UserService
import play.api.mvc.{ Action, AnyContent, Controller }
import play.api.{ Configuration, Environment, Mode }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Authentication @Inject()(
  userService: UserService,
  configuration: Configuration,
  environment: Environment,
  auth: Authenticate) extends Controller {

  def login = Action { implicit request =>
    if (environment.mode == Mode.Prod) {
      val clientId: String = configuration.getString("google.clientId").get
      Ok(views.html.login(clientId)).withSession("state" -> auth.generateState)
    } else {
      Redirect(routes.Application.index()).withSession("email" -> "developer@lunatech.com")
    }
  }

  def authenticate(code: String, idToken: String, accessToken: String): Action[AnyContent] = Action.async {
    val response = auth.authenticateToken(code, idToken, accessToken)

    response.flatMap {
      case Left(parameters) =>
        userService.addIfNew(emailAddress = parameters.head._2).map(_ =>
          Redirect(routes.Application.index()).withSession(parameters.toArray: _*))
      case Right(message) => Future.successful(Redirect(routes.Authentication.login())
        .withNewSession.flashing("error" -> message.toString()))
    }
  }

  def logout = Action {
    Redirect(routes.Authentication.login()).withNewSession.flashing("success" -> "You've been logged out")
  }
}
