package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuDish, MenuWithNamePerDay }
import lunatech.lunchplanner.persistence.{ MenuDishTable, UserTable }
import lunatech.lunchplanner.services.{ DishService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ DishForm, MenuForm, MenuPerDayForm }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuController  @Inject() (
  userService: UserService,
  dishService: DishService,
  menuService: MenuService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def getAllMenus(activePage: Int) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
        menus <- menuService.getAllMenusWithListOfDishes.map(_.toArray)
      } yield
        Ok(views.html.admin.menus(activePage, currentUser.get, MenuForm.menuForm, dishes, menus))
    }
  }

  def createNewMenu = IsAdminAsync { username =>
    implicit request => {

      for {
        user <- userService.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
        menus <- menuService.getAllMenusWithListOfDishes.map(_.toArray)
        result <- MenuForm
          .menuForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(views.html.admin.menus(
              activeTab = 0,
              user.get,
              formWithErrors,
              dishes,
              menus
            ))),
            menuData => {
              addNewMenuDishes(menuData).map(_ =>
                Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus()))
            })
      } yield result
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
