package lunatech.lunchplanner.controllers

import com.lunatech.openconnect.GoogleSecured
import lunatech.lunchplanner.models.User
import play.api.mvc.{ControllerComponents, Request, Result, Results}
import play.api.Configuration

trait Secured extends GoogleSecured {

  val configuration: Configuration
  val controllerComponents: ControllerComponents

  override def onUnauthorized[A](request: Request[A]): Result =
    Results.Redirect(routes.Authentication.login)

  override def onForbidden[A](request: Request[A]): Result =
    Results
      .Redirect(routes.Application.index)
      .flashing("error" -> "You are not an admin!")

  def getCurrentUser(optionUser: Option[User],
                     isAdmin: Boolean,
                     emailAddress: String): User = {
    val user = optionUser.map(_.copy(isAdmin = isAdmin))
    user.getOrElse(User(name = "", emailAddress = emailAddress))
  }
}
