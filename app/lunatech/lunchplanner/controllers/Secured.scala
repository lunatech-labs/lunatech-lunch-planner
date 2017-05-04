package lunatech.lunchplanner.controllers

import com.lunatech.openconnect.GoogleSecured
import play.api.mvc.{ RequestHeader, Result, Results }
import play.api.{ Configuration, Environment }

trait Secured extends GoogleSecured {

  val configuration: Configuration
  val environment: Environment

  override def onUnauthorized(request: RequestHeader) =
    Results.Redirect(lunatech.lunchplanner.controllers.routes.Authentication.login())

  override def onForbidden(request: RequestHeader): Result = Results.Forbidden("YOU ARE NOT ADMIN!!!")

}
