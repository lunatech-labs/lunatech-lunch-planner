package lunatech.lunchplanner.controllers

import java.text.SimpleDateFormat
import java.util.UUID
import java.util.Date

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.{ MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ ListMenusPerDayForm, MenuPerDayForm }
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

  def getAllMenusPerDay = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
      } yield
        Ok(views.html.admin.menuPerDay.menusPerDay(
          currentUser.get,
          ListMenusPerDayForm.listMenusPerDayForm,
          menusPerDay))
    }
  }

  def createNewMenuPerDay = IsAdminAsync { username =>
    val currentDate =  new SimpleDateFormat("dd-MM-yyyy").format(new Date())
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
              currentDate,
              formWithErrors,
              menusUuidAndNames))},
          menuPerDayData => {
            menuPerDayService.add(menuPerDayData).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay))
          })
    }
  }

  def getNewMenuPerDay = IsAdminAsync { username =>
    val currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date())
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
      } yield
        Ok(views.html.admin.menuPerDay.newMenuPerDay(currentUser.get, currentDate, MenuPerDayForm.menuPerDayForm, menusUuidAndNames))
    }
  }

  def deleteMenusPerDay = IsAdminAsync { username =>
    implicit request => {
      ListMenusPerDayForm
        .listMenusPerDayForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
            } yield BadRequest(
              views.html.admin.menuPerDay.menusPerDay(currentUser.get, formWithErrors, menusPerDay))},
          menusPerDayData =>
            delete(menusPerDayData).map( _ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay))
        )
    }
 }

  def getMenuPerDayDetails(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
        menuPerDayOption <- menuPerDayService.getMenuPerDayByUuid(uuid)
      } yield
        Ok(views.html.admin.menuPerDay.menuPerDayDetails(
          currentUser.get,
          MenuPerDayForm.menuPerDayForm,
          menusUuidAndNames,
          menuPerDayOption))
    }
  }

  def deleteMenuPerDay(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      MenuPerDayForm
        .menuPerDayForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              menusUuidAndNames <- menuService.getAllMenusUuidAndNames
              menuPerDayOption <- menuPerDayService.getMenuPerDayByUuid(uuid)
            } yield BadRequest(views.html.admin.menuPerDay.menuPerDayDetails(
              currentUser.get,
              formWithErrors,
              menusUuidAndNames,
              menuPerDayOption))},
          _ => {
            menuPerDayService.delete(uuid).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay))
          })
    }
  }

  def saveMenuPerDayDetails(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      MenuPerDayForm
        .menuPerDayForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              menusUuidAndNames <- menuService.getAllMenusUuidAndNames
              menuPerDayOption <- menuPerDayService.getMenuPerDayByUuid(uuid)
            } yield BadRequest(views.html.admin.menuPerDay.menuPerDayDetails(currentUser.get, formWithErrors, menusUuidAndNames, menuPerDayOption))},
          menuPerDayData => {
            menuPerDayService.insertOrUpdate(uuid, menuPerDayData).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay))
          })
    }
  }

  private def delete(form: ListMenusPerDayForm) =
    Future.sequence(form.listUuids.map(uuid => menuPerDayService.delete(uuid)))

}
