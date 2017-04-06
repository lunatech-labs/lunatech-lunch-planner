package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.{ DishService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.MenuPerDayForm
import play.api.data.Form
import play.api.data.Forms.{ mapping, of, _ }
import play.api.data.format.Formats._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global

class MenuPerDayController  @Inject() (
  userService: UserService,
  dishService: DishService,
  menuService: MenuService,
  menuPerDayService: MenuPerDayService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def createNewMenuPerDay() = IsAdminAsync { username =>
    implicit request => {
      val currentUser = userService.getUserByEmailAddress(username)
      val allDishes = dishService.getAllDishes.map(_.toArray)
      val allMenus = menuService.getAllMenus.map(_.toArray)
      val allMenusUuidsAndNames = menuService.getAllMenusUuidAndNames
      currentUser.flatMap(user =>
        allDishes.flatMap(dishes =>
          allMenus.flatMap(menus =>
            allMenusUuidsAndNames.map(menusUuidAndNames =>
            MenuPerDayController
              .menuPerDayForm
              .bindFromRequest
              .fold(
                formWithErrors => BadRequest(views.html.admin(
                  user.get,
                  DishController.dishForm,
                  MenuController.menuForm,
                  dishes,
                  menus,
                  formWithErrors,
                  menusUuidAndNames)),
                menuPerDayData => {
                  menuPerDayService.addNewMenuPerDay(menuPerDayData)
                  Redirect(lunatech.lunchplanner.controllers.routes.Application.admin())
                })))))
    }
  }

}

object MenuPerDayController {
  val menuPerDayForm = Form(
    mapping(
      "menuUuid" -> of[UUID],
      "date" -> nonEmptyText
    )(MenuPerDayForm.apply)(MenuPerDayForm.unapply)
  )
}
