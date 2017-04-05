package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import com.lunatech.openconnect.Authenticate
import play.api.mvc.{ Action, Controller }
import play.api.{ Configuration, Environment, Mode }

import scala.concurrent.ExecutionContext.Implicits.global

class Authentication @Inject()(configuration: Configuration, environment: Environment, auth: Authenticate) extends Controller {

  /**
    * Login page.
    */
  def login = Action { implicit request =>
//    if (environment.mode == Mode.Prod) {
      val clientId: String = configuration.getString("google.clientId").get
      Ok(views.html.login(clientId)).withSession("state" -> auth.generateState)
//    } else {
//      Redirect(routes.Application.index()).withSession("email" -> "leonor.boga@lunatech.com")
//    }
  }

  def authenticate(code: String, idToken: String, accessToken: String) = Action.async {
    val response = auth.authenticateToken(code, idToken, accessToken)

    response.map {
      case Left(parameters) => Redirect(routes.Application.index()).withSession(parameters.toArray: _*)
      case Right(message) => Redirect(routes.Authentication.login()).withNewSession.flashing("error" -> message.toString())
    }
  }

  /**
    * Logout and clean the session.
    */
  def logout = Action {
    Redirect(routes.Authentication.login()).withNewSession.flashing("success" -> "You've been logged out")
  }
}
