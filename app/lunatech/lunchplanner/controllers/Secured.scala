package lunatech.lunchplanner.controllers

import com.lunatech.openconnect.GoogleSecured
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.UserService
import play.api.mvc.{ RequestHeader, Result, Results, _ }
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Provide security features
  */
trait Secured extends GoogleSecured {

  val configuration: Configuration
  val environment: Environment
  val connection: DBConnection
  val userService: UserService = new UserService(configuration, connection)

  override def IsAdminAsync(f: String => Request[AnyContent] => Future[Result]) = IsAuthenticatedAsync { userEmailAddress =>
    request =>
      val isUserAdmin = userService.isAdminUser(userEmailAddress)
        if (isUserAdmin) {
          f(userEmailAddress)(request)
        } else {
          Future.successful(Results.Forbidden("you are not admin"))
        }
  }

  override def onUnauthorized(request: RequestHeader) =
    Results.Redirect(lunatech.lunchplanner.controllers.routes.Authentication.login())

  override def onForbidden(request: RequestHeader): Result = Results.Forbidden("YOU ARE NOT ADMIN!!!")

}
