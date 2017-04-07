package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.{ DishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.MenuPerDayForm
import play.api.data.Form
import play.api.data.Forms.{ mapping, of, _ }
import play.api.data.format.Formats._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayController  @Inject() (
  userService: UserService,
  dishService: DishService,
  menuService: MenuService,
  menuPerDayService: MenuPerDayService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def createNewMenuPerDay() = IsAdminAsync { username =>
    implicit request => {

      for {
        user <- userService.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
        menus <- menuService.getAllMenus.map(_.toArray)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
        menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
        result <- MenuPerDayController
          .menuPerDayForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(views.html.admin(
              activeTab = 2,
              user.get,
              DishController.dishForm,
              MenuController.menuForm,
              dishes,
              menus,
              formWithErrors,
              menusUuidAndNames,
              menusPerDay))),
            menuPerDayData => {
              menuPerDayService.addNewMenuPerDay(menuPerDayData).map(_ =>
                Redirect(lunatech.lunchplanner.controllers.routes.Application.admin(activePage = 2)))
            })
      } yield result
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
