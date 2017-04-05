package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.UserTable
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def index = IsAuthenticatedAsync { username =>
    implicit request =>
      val currentUser = UserTable.getUserByEmailAddress(username)
      getIndexPage(currentUser)
  }

  def admin =
    IsAdminAsync { username =>
      implicit request =>
        val userAdmin = UserTable.getUserByEmailAddress(username)
        getAdminPage(userAdmin)
    }

  private def getAdminPage(adminUser: Future[Option[User]])=
    adminUser.map {
      case Some(user) => Ok(views.html.admin(user))
      case None => Unauthorized
    }

  private def getIndexPage(normalUser: Future[Option[User]])=
    normalUser.map {
      case Some(user) => Ok(views.html.index(user))
      case None => Unauthorized
    }

}
