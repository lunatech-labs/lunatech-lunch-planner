package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Menu, MenuDish }
import lunatech.lunchplanner.services.{ DishService, MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ ListMenusForm, MenuForm }
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
        currentUser <- userService.getByEmailAddress(username)
        menus <- menuDishService.getAllWithListOfDishes.map(_.toArray)
      } yield
        Ok(views.html.admin.menu.menus(
          getCurrentUser(currentUser, isAdmin = true, username),
          ListMenusForm.listMenusForm, menus))
    }
  }

  def createNewMenu = IsAdminAsync { username =>
    implicit request => {
      MenuForm
      .menuForm
      .bindFromRequest
      .fold(
        formWithErrors => {
          for {
            currentUser <- userService.getByEmailAddress(username)
            dishes <- dishService.getAll.map(_.toArray)
          } yield BadRequest(views.html.admin.menu.newMenu(
            getCurrentUser(currentUser, isAdmin = true, username),
          formWithErrors,
          dishes))},
        menuData => {
          addNewMenuDishes(menuData).map(_ =>
            Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus())
              .flashing("success" -> "New menu created!"))
        })
    }
  }

  def getNewMenu = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        dishes <- dishService.getAll.map(_.toArray)
      } yield
        Ok(views.html.admin.menu.newMenu(
          getCurrentUser(currentUser, isAdmin = true, username),
          MenuForm.menuForm, dishes))
    }
  }

  def getMenuDetails(menuUuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menuDish <- menuDishService.getByUuidWithSelectedDishes(menuUuid)
      } yield
        Ok(views.html.admin.menu.menuDetails(
          getCurrentUser(currentUser, isAdmin = true, username),
          MenuForm.menuForm, menuDish))
    }
  }

  def saveMenuDetails(menuUuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      MenuForm
        .menuForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              menuDish <- menuDishService.getByUuidWithSelectedDishes(menuUuid)
          } yield BadRequest(views.html.admin.menu.menuDetails(
              getCurrentUser(currentUser, isAdmin = true, username),
              formWithErrors, menuDish))},
          menuData => {
            updateMenuDishes(menuUuid, menuData).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus())
                .flashing("success" -> "Menu updated!"))
          })
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
              currentUser <- userService.getByEmailAddress(username)
              menuDish <- menuDishService.getByUuidWithSelectedDishes(menuUuid)
            } yield BadRequest(views.html.admin.menu.menuDetails(getCurrentUser(
              currentUser, isAdmin = true, username),
              formWithErrors, menuDish))
          },
          _ => {
            deleteMenuDish(menuUuid).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus())
                .flashing("success" -> "Menu deleted!"))
          })
    }
  }

  def deleteMenus() = IsAdminAsync { username =>
    implicit request => {
      ListMenusForm
        .listMenusForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              menus <- menuDishService.getAllWithListOfDishes.map(_.toArray)
            } yield BadRequest(
            views.html.admin.menu.menus(
              getCurrentUser(currentUser, isAdmin = true, username),
              formWithErrors, menus))},
          menusData =>
            deleteMenuDishes(menusData).map( _ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus())
                .flashing("success" -> "Menu(s) deleted!"))
        )
    }
  }

  private def addNewMenuDishes(menuData: MenuForm) = {
    // add new menu
    val newMenu = Menu(name = menuData.menuName)
   menuService.add(newMenu)
     .flatMap(menu =>
      //Add MenuDishes
       addMenuDishes(menu.uuid, menuData.dishesUuid))
  }

  private def updateMenuDishes(menuUuid: UUID, menuData: MenuForm) = {
    // update menu name
    val menu = Menu(name = menuData.menuName)
    menuService.insertOrUpdate(menuUuid, menu)

    // remove all previous menu dishes and add them again
    menuDishService.deleteByMenuUuid(menuUuid)
    addMenuDishes(menuUuid, menuData.dishesUuid)
  }

  private def addMenuDishes(menuUuid: UUID, dishesUuid: List[UUID]) =
    Future.sequence(dishesUuid.map { uuid =>
      val newMenuDish = MenuDish(menuUuid = menuUuid, dishUuid = uuid)
      menuDishService.add(newMenuDish)
    })

  private def deleteMenuDish(menuUuid: UUID) = {
    for {
      menusPerDay <- menuPerDayService.getAllByMenuUuid(menuUuid)
      _ <- Future.sequence(menusPerDay.map(mpd => menuPerDayPerPersonService.deleteByMenuPerPersonUuid(mpd.uuid)))
      _ <- menuPerDayService.deleteByMenuUuid(menuUuid)
      _ <- menuDishService.deleteByMenuUuid(menuUuid)
      result <- menuService.delete(menuUuid)
    } yield result
  }

  private def deleteMenuDishes(listMenuForm: ListMenusForm) =
    Future.sequence(listMenuForm.listUuids.map(uuid => deleteMenuDish(uuid)))
}
