package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.services.{ DishService, UserService }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Controller, Result }
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (
  userService: UserService,
  dishService: DishService,
  val connection: DBConnection,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration)
  extends Controller with Secured with I18nSupport {

  def index = IsAuthenticatedAsync { username =>
    implicit request =>
      val currentUser = userService.getUserByEmailAddress(username)
      getIndexPage(currentUser)
  }

  def admin =
    IsAdminAsync { username =>
      implicit request =>
        val userAdmin = userService.getUserByEmailAddress(username)
        getAdminPage(userAdmin)
    }

  private def getAdminPage(adminUser: Future[Option[User]]): Future[Result] =
    adminUser.flatMap {
      case Some(user) =>
        val allDishes = dishService.getAllDishes.map(_.toArray)
        allDishes.map(dishes =>
          Ok(views.html.admin(user, DishController.dishForm, MenuController.menuForm, dishes)))
      case None => Future.successful(Unauthorized)
    }

  private def getIndexPage(normalUser: Future[Option[User]]) =
    normalUser.map {
      case Some(user) => Ok(views.html.index(user))
      case None => Unauthorized
    }

}
