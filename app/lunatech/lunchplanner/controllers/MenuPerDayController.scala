package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.{ MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ ListMenusForm, ListMenusPerDayForm, MenuForm, MenuPerDayForm }
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

  private def delete(form: ListMenusPerDayForm) =
    Future.sequence(form.listUuids.map(uuid => menuPerDayService.delete(uuid)))

}
