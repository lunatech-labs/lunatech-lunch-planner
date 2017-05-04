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
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  val connection: DBConnection,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration)
  extends Controller with Secured with I18nSupport {

  def index = IsAuthenticatedAsync { username =>
    implicit request =>
      userService.getUserByEmailAddress(username).flatMap(currentUser =>
        getIndexPage(currentUser))
  }

  def admin(activePage: Int) =
    IsAdminAsync { username =>
      implicit request =>
        userService.getUserByEmailAddress(username).flatMap(userAdmin =>
          getAdminPage(userAdmin, activePage))
    }

  private def getAdminPage(adminUser: Option[User], activePage: Int): Future[Result] =
    adminUser match {
      case Some(user) =>
        for {
          dishes <- dishService.getAllDishes.map(_.toArray)
          menus <- menuService.getAllMenusWithListOfDishes.map(_.toArray)
          menusUuidAndNames <- menuService.getAllMenusUuidAndNames
          menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
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

  private def getIndexPage(normalUser: Option[User]) =
    normalUser match {
      case Some(user) =>
        val userIsAdmin = userService.isAdminUser(user.emailAddress)
        val allMenusPerDayPerPersonAndSelected = menuPerDayPerPersonService.getAllMenuWithNamePerDayWithDishesPerPerson(user.uuid).map(_.toArray)
        allMenusPerDayPerPersonAndSelected.map(menusPerDayPerPerson =>
          Ok(views.html.index(user, userIsAdmin, menusPerDayPerPerson, MenuPerDayPerPersonController.menuPerDayPerPersonForm)))
      case None => Future.successful(Unauthorized)
    }

}
