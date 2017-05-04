package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuDish
import lunatech.lunchplanner.persistence.MenuDishTable
import lunatech.lunchplanner.services.{ DishService, MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ DishForm, ListMenusForm, MenuForm }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuController  @Inject() (
  userService: UserService,
  dishService: DishService,
  menuService: MenuService,
  menuPerDayService: MenuPerDayService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  menuDishService: MenuDishService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def getAllMenus = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getUserByEmailAddress(username)
        menus <- menuDishService.getAllMenusWithListOfDishes.map(_.toArray)
      } yield
        Ok(views.html.admin.menu.menus(currentUser.get, ListMenusForm.listMenusForm, menus))
    }
  }

  def createNewMenu = IsAdminAsync { username =>
    implicit request => {
      for {
        currentUser <- userService.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
        result <- MenuForm
          .menuForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(views.html.admin.menu.newMenu(
              currentUser.get,
              formWithErrors,
              dishes
            ))),
            menuData => {
              addNewMenuDishes(menuData).map(_ =>
                Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus()))
            })
      } yield result
    }
  }

  def getNewMenu = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
      } yield
        Ok(views.html.admin.menu.newMenu(currentUser.get, MenuForm.menuForm, dishes))
    }
  }

  def getMenuDetails(menuUuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getUserByEmailAddress(username)
        menuDish <- menuDishService.getMenuDishByUuidWithSelectedDishes(menuUuid)
      } yield
        Ok(views.html.admin.menu.menuDetails(currentUser.get, MenuForm.menuForm, menuDish))
    }
  }

  def saveMenuDetails(menuUuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for {
        currentUser <- userService.getUserByEmailAddress(username)
        menuDish <- menuDishService.getMenuDishByUuidWithSelectedDishes(menuUuid)
        result <- MenuForm
          .menuForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(views.html.admin.menu.menuDetails(currentUser.get, formWithErrors, menuDish))),
            menuData => {
              updateMenuDishes(menuUuid, menuData).map(_ =>
                Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus()))
            })
      } yield result
    }
  }

  def deleteMenu(menuUuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      MenuForm
        .menuForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getUserByEmailAddress(username)
              menuDish <- menuDishService.getMenuDishByUuidWithSelectedDishes(menuUuid)
            } yield BadRequest(views.html.admin.menu.menuDetails(currentUser.get, formWithErrors, menuDish))
          },
          _ => {
            deleteMenuDish(menuUuid).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus()))
          })
    }
  }

  def deleteMenus = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getUserByEmailAddress(username)
        menus <- menuDishService.getAllMenusWithListOfDishes.map(_.toArray)
        result <- ListMenusForm
          .listMenusForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(
              views.html.admin.menu.menus(currentUser.get, formWithErrors, menus))),
            menusData =>
              deleteMenuDishes(menusData).map( _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus()))
          )
      } yield result
    }
  }

  private def addNewMenuDishes(menuData: MenuForm) = {
    // add new menu
   menuService.addNewMenu(menuData)
     .flatMap(menu =>
      //Add MenuDish
       Future.sequence(menuData.dishesUuid.map { uuid =>
        val newMenuDish = MenuDish(menuUuid = menu.uuid, dishUuid = uuid)
        MenuDishTable.addMenuDish(newMenuDish)
      }))
  }

  private def updateMenuDishes(menuUuid: UUID, menuData: MenuForm) = {
    // update menu name
    menuService.insertOrUpdateMenu(menuUuid, menuData)

    // remove all previous menu dishes and add them again
    menuDishService.deleteMenuDishesByMenuUuid(menuUuid)
    Future.sequence(menuData.dishesUuid.map { uuid =>
      val newMenuDish = MenuDish(menuUuid = menuUuid, dishUuid = uuid)
      MenuDishTable.addMenuDish(newMenuDish)
    })
  }

  private def deleteMenuDish(menuUuid: UUID) = {
    for {
      menusPerDay <- menuPerDayService.getAllMenusPerDayByMenuUuid(menuUuid)
      _ <- Future.sequence(menusPerDay.map(mpd => menuPerDayPerPersonService.deleteMenusPerDayPerPersonByMenuPerPersonUuid(mpd.uuid)))
      _ <- menuPerDayService.deleteMenuPerDayByMenuUuid(menuUuid)
      _ <- menuDishService.deleteMenuDishesByMenuUuid(menuUuid)
      result <- menuService.deleteMenu(menuUuid)
    } yield result
  }

  private def deleteMenuDishes(listMenuForm: ListMenusForm) =
    Future.sequence(listMenuForm.listUuids.map(uuid => deleteMenuDish(uuid)))
}
