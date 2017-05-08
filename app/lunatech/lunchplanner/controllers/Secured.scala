package lunatech.lunchplanner.controllers

import com.lunatech.openconnect.GoogleSecured
import lunatech.lunchplanner.models.User
import play.api.mvc.{ RequestHeader, Result, Results }
import play.api.{ Configuration, Environment }

trait Secured extends GoogleSecured {

  val configuration: Configuration
  val environment: Environment

  override def onUnauthorized(request: RequestHeader) =
    Results.Redirect(lunatech.lunchplanner.controllers.routes.Authentication.login())

  override def onForbidden(request: RequestHeader): Result = Results.Forbidden("YOU ARE NOT ADMIN!!!")

  def getCurrentUser(optionUser: Option[User], isAdmin: Boolean, emailAddress: String): User = {
    val user = optionUser.map(_.copy(isAdmin = isAdmin))
    user.getOrElse(User(emailAddress = emailAddress))
  }

}
