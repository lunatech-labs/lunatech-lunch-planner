package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.{ MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.MenuPerDayForm
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayController  @Inject() (
  userService: UserService,
  menuService: MenuService,
  menuDishService: MenuDishService,
  menuPerDayService: MenuPerDayService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def getAllMenusPerDay(activePage: Int) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menus <- menuDishService.getAllWithListOfDishes.map(_.toArray)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
        menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
      } yield
        Ok(views.html.admin.menusPerDay(
          activePage,
          currentUser.get,
          MenuPerDayForm.menuPerDayForm,
          menus,
          menusUuidAndNames,
          menusPerDay))
    }
  }

  def createNewMenuPerDay() = IsAdminAsync { username =>
    implicit request => {

      for {
        user <- userService.getByEmailAddress(username)
        menus <- menuDishService.getAllWithListOfDishes.map(_.toArray)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
        menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
        result <- MenuPerDayForm
          .menuPerDayForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(views.html.admin.menusPerDay(
              activeTab = 0,
              user.get,
              formWithErrors,
              menus,
              menusUuidAndNames,
              menusPerDay))),
            menuPerDayData => {
              menuPerDayService.add(menuPerDayData).map(_ =>
                Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay()))
            })
      } yield result
    }
  }

}
