package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.{ MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ MenuForm, MenuPerDayForm }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global

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

  def getAllMenusPerDay = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menus <- menuDishService.getAllWithListOfDishes.map(_.toArray)
        menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
      } yield
        Ok(views.html.admin.menuPerDay.menusPerDay(
          currentUser.get,
          MenuPerDayForm.menuPerDayForm,
          menus,
          menusPerDay))
    }
  }

  def createNewMenuPerDay = IsAdminAsync { username =>
    implicit request => {
      MenuPerDayForm
        .menuPerDayForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              user <- userService.getByEmailAddress(username)
              menusUuidAndNames <- menuService.getAllMenusUuidAndNames
            } yield BadRequest(views.html.admin.menuPerDay.newMenuPerDay(
              user.get,
              formWithErrors,
              menusUuidAndNames))},
          menuPerDayData => {
            menuPerDayService.add(menuPerDayData).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay()))
          })
    }
  }

  def getNewMenuPerDay = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
      } yield
        Ok(views.html.admin.menuPerDay.newMenuPerDay(currentUser.get, MenuPerDayForm.menuPerDayForm, menusUuidAndNames))
    }
  }

}
