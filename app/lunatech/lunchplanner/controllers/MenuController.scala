package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Menu, MenuDish, MenuWithNamePerDay }
import lunatech.lunchplanner.persistence.{ MenuDishTable, MenuTable }
import lunatech.lunchplanner.services.{ DishService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.MenuForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global

class MenuController  @Inject() (
  userService: UserService,
  dishService: DishService,
  menuService: MenuService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def createNewMenu() = IsAdminAsync { username =>
    implicit request => {

      for{
        user <- userService.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
        menus <- menuService.getAllMenus.map(_.toArray)
      } yield
        MenuController
          .menuForm
          .bindFromRequest
          .fold(
            formWithErrors => BadRequest(views.html.admin(
              user.get,
              DishController.dishForm,
              formWithErrors,
              dishes,
              menus,
              MenuPerDayController.menuPerDayForm,
              Seq.empty[(String, String)],
              Array.empty[MenuWithNamePerDay])),
            menuData => {
              addNewMenuDishes(menuData)
              Redirect(lunatech.lunchplanner.controllers.routes.Application.admin())
            })
    }
  }

  private def addNewMenuDishes(menuData: MenuForm) = {
    // add new menu
    val newMenu = menuService.addNewMenu(menuData)

    newMenu.map( menu =>
      //Add MenuDish
      for(dishUuid <- menuData.dishesUuid) {
        val newMenuDish = MenuDish(menuUuid = menu.uuid, dishUuid = dishUuid)
        MenuDishTable.addMenuDish(newMenuDish)
      }
    )
  }

  def removeMenuDish() = ???
  def filterDishByMenuName = ???
  def filterDishByUUID = ???
  def filterDishByMenuUUID = ???
}

object MenuController {
  val menuForm = Form(
    mapping(
      "menuName" -> nonEmptyText,
      "dishesUuid" -> list(of[UUID])
    )(MenuForm.apply)(MenuForm.unapply)
  )

}
