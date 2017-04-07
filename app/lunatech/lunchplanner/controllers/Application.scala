package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.services.{ DishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Controller, Result }
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (
  userService: UserService,
  dishService: DishService,
  menuService: MenuService,
  menuPerDayService: MenuPerDayService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
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

  def admin(activePage: Int) =
    IsAdminAsync { username =>
      implicit request =>
        val userAdmin = userService.getUserByEmailAddress(username)
        getAdminPage(userAdmin, activePage)
    }

  private def getAdminPage(adminUser: Future[Option[User]], activePage: Int): Future[Result] =
    adminUser.flatMap {
      case Some(user) =>
        val allDishes = dishService.getAllDishes.map(_.toArray)
        val allMenus = menuService.getAllMenus.map(_.toArray)
        val allMenusUuidsAndNames = menuService.getAllMenusUuidAndNames
        val allMenusPerDay = menuPerDayService.getAllMenuWithNamePerDay.map(_.toArray)

        for {
          dishes <- allDishes
          menus <- allMenus
          menusUuidAndNames <- allMenusUuidsAndNames
          menusPerDay <- allMenusPerDay
        } yield
          Ok(views.html.admin(
            activePage,
            user,
            DishController.dishForm,
            MenuController.menuForm,
            dishes,
            menus,
            MenuPerDayController.menuPerDayForm,
            menusUuidAndNames,
            menusPerDay))
      case None => Future.successful(Unauthorized)
    }

  private def getIndexPage(normalUser: Future[Option[User]]) =
    normalUser.flatMap {
      case Some(user) =>
        val allMenusPerDayPerPersonAndSelected = menuPerDayPerPersonService.getAllMenuWithNamePerDayPerPerson(user.uuid).map(_.toArray)
        allMenusPerDayPerPersonAndSelected.map(menusPerDayPerPerson =>
          Ok(views.html.index(user, menusPerDayPerPerson, MenuPerDayPerPersonController.menuPerDayPerPersonForm)))
      case None => Future.successful(Unauthorized)
    }

}
