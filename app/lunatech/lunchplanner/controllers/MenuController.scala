package lunatech.lunchplanner.controllers

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models._
import lunatech.lunchplanner.services.{ DishService, MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ ListMenusForm, MenuForm }
import play.api.i18n.I18nSupport
import play.api.mvc.{ BaseController, ControllerComponents }
import play.api.{ Configuration, Environment }

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuController @Inject()(
    userService: UserService,
    dishService: DishService,
    menuService: MenuService,
    menuPerDayService: MenuPerDayService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    menuDishService: MenuDishService,
    val controllerComponents: ControllerComponents,
    val environment: Environment,
    val configuration: Configuration)(implicit val connection: DBConnection)
    extends BaseController
    with Secured
    with I18nSupport {

  def getAllMenus = adminAction.async { implicit request =>
    for {
      currentUser <- userService.getByEmailAddress(request.email)
      menus <- menuDishService.getAllWithListOfDishes.map(_.toArray)
    } yield
      Ok(
        views.html.admin.menu.menus(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          ListMenusForm.listMenusForm,
          menus))
  }

  def createNewMenu = adminAction.async { implicit request =>
    MenuForm.menuForm.bindFromRequest
      .fold(
        formWithErrors => {
          for {
            currentUser <- userService.getByEmailAddress(request.email)
            dishes <- dishService.getAll.map(_.toArray)
          } yield
            BadRequest(
              views.html.admin.menu.newMenu(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                formWithErrors,
                dishes))
        },
        menuData => {
          addNewMenuDishes(menuData).map(
            _ =>
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus)
                .flashing("success" -> "New menu created!"))
        }
      )
  }

  def getNewMenu = adminAction.async { implicit request =>
    for {
      currentUser <- userService.getByEmailAddress(request.email)
      dishes <- dishService.getAll.map(_.toArray)
    } yield
      Ok(
        views.html.admin.menu.newMenu(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          MenuForm.menuForm,
          dishes))
  }

  def getMenuDetails(menuUuid: UUID) = adminAction.async { implicit request =>
    for {
      currentUser <- userService.getByEmailAddress(request.email)
      menuDish <- menuDishService.getByUuidWithSelectedDishes(menuUuid)
    } yield
      Ok(
        views.html.admin.menu.menuDetails(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          MenuForm.menuForm,
          menuDish))
  }

  def saveMenuDetails(menuUuid: UUID) = adminAction.async { implicit request =>
    MenuForm.menuForm.bindFromRequest
      .fold(
        formWithErrors => {
          for {
            currentUser <- userService.getByEmailAddress(request.email)
            menuDish <- menuDishService.getByUuidWithSelectedDishes(menuUuid)
          } yield
            BadRequest(
              views.html.admin.menu.menuDetails(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                formWithErrors,
                menuDish))
        },
        menuData => {
          updateMenuDishes(menuUuid, menuData).map(
            _ =>
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus)
                .flashing("success" -> "Menu updated!"))
        }
      )
  }

  def deleteMenu(menuUuid: UUID) = adminAction.async { implicit request =>
    MenuForm.menuForm.bindFromRequest
      .fold(
        formWithErrors => {
          for {
            currentUser <- userService.getByEmailAddress(request.email)
            menuDish <- menuDishService.getByUuidWithSelectedDishes(menuUuid)
          } yield
            BadRequest(
              views.html.admin.menu.menuDetails(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                formWithErrors,
                menuDish))
        },
        _ => {
          deleteMenuDish(menuUuid).map(
            _ =>
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus)
                .flashing("success" -> "Menu deleted!"))
        }
      )
  }

  def deleteMenus() = adminAction.async { implicit request =>
    ListMenusForm.listMenusForm.bindFromRequest
      .fold(
        formWithErrors => {
          for {
            currentUser <- userService.getByEmailAddress(request.email)
            menus <- menuDishService.getAllWithListOfDishes.map(_.toArray)
          } yield
            BadRequest(
              views.html.admin.menu.menus(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                formWithErrors,
                menus))
        },
        menusData =>
          deleteMenuDishes(menusData).map(
            _ =>
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuController.getAllMenus)
                .flashing("success" -> "Menu(s) deleted!"))
      )
  }

  private def addNewMenuDishes(menuData: MenuForm) = {
    // add new menu
    val newMenu = Menu(name = menuData.menuName.normalize)
    menuService
      .add(newMenu)
      .flatMap(
        menu =>
          //Add MenuDishes
          addMenuDishes(menu.uuid, menuData.dishesUuid))
  }

  private def updateMenuDishes(menuUuid: UUID, menuData: MenuForm) = {
    // update menu name
    val menu = Menu(name = menuData.menuName.normalize)
    menuService.update(menuUuid, menu)

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
      _ <- Future.sequence(menusPerDay.map { mpd =>
        val menuDate: LocalDate = mpd.date.toLocalDate
        val currentDate = LocalDate.now

        // do not delete past menus
        if (menuDate.isAfter(currentDate)) {
          menuPerDayPerPersonService.deleteByMenuPerPersonUuid(mpd.uuid)
        } else {
          Future.successful(0)
        }
      })
      _ <- menuPerDayService.deleteByMenuUuid(menuUuid)
      _ <- menuDishService.deleteByMenuUuid(menuUuid)
      result <- menuService.delete(menuUuid)
    } yield result
  }

  private def deleteMenuDishes(listMenuForm: ListMenusForm) =
    Future.sequence(listMenuForm.listUuids.map(uuid => deleteMenuDish(uuid)))
}
