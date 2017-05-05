package lunatech.lunchplanner.controllers

import java.text.SimpleDateFormat
import java.util.UUID
import java.util.Date

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuWithNamePerDay
import lunatech.lunchplanner.services.{ MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import lunatech.lunchplanner.viewModels.{ FilterMenusPerDayForm, ListMenusPerDayForm, MenuPerDayForm }
import org.joda.time.DateTime
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayController @Inject() (
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

  def getAllMenusPerDay(dateStart: Option[String] = None, dateEnd: Option[String] = None) = IsAdminAsync { username =>
    val dStart = dateStart.map(java.sql.Date.valueOf(_)).getOrElse(getDateStart)
    val dEnd = dateEnd.map(java.sql.Date.valueOf(_)).getOrElse(getDateEnd)

    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDayFilterDateRange(
          dStart,
          dEnd)
            .map(_.toArray)
      } yield
        Ok(views.html.admin.menuPerDay.menusPerDay(
          currentUser.get,
          new SimpleDateFormat("dd-MM-yyyy").format(dStart),
          new SimpleDateFormat("dd-MM-yyyy").format(dEnd),
          ListMenusPerDayForm.listMenusPerDayForm,
          menusPerDay))
    }
  }

  def filterMenusPerDay = IsAdminAsync { username =>
    implicit request => {
      FilterMenusPerDayForm
        .filterMenusPerDayForm
        .bindFromRequest
        .fold(
          _ => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay.map(_.toArray)
            } yield BadRequest(views.html.admin.menuPerDay.menusPerDay(
              currentUser.get,
              new SimpleDateFormat("dd-MM-yyyy").format(getDateStart),
              new SimpleDateFormat("dd-MM-yyyy").format(getDateEnd),
              ListMenusPerDayForm.listMenusPerDayForm,
              menusPerDay))},
          filterDataForm => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              menusPerDay <- getAllFilterByDateRange(filterDataForm).map(_.toArray)
            } yield Ok(views.html.admin.menuPerDay.menusPerDay(
              currentUser.get,
              new SimpleDateFormat("dd-MM-yyyy").format(filterDataForm.dateStart),
              new SimpleDateFormat("dd-MM-yyyy").format(filterDataForm.dateEnd),
              ListMenusPerDayForm.listMenusPerDayForm,
              menusPerDay))
          })
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
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay()))
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
              views.html.admin.menuPerDay.menusPerDay(
                currentUser.get,
                new SimpleDateFormat("dd-MM-yyyy").format(getDateStart),
                new SimpleDateFormat("dd-MM-yyyy").format(getDateEnd),
                formWithErrors,
                menusPerDay))},
          menusPerDayData =>
            deleteSeveral(menusPerDayData).map( _ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay(
                Some(new SimpleDateFormat("yyyy-MM-dd").format(menusPerDayData.dateStart)),
                Some(new SimpleDateFormat("yyyy-MM-dd").format(menusPerDayData.dateEnd)))))
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
            delete(uuid).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay(None, None)))
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
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay(None, None)))
          })
    }
  }

  private def deleteSeveral(form: ListMenusPerDayForm) =
    Future.sequence(form.listUuids.map(uuid => delete(uuid)))

  private def delete(uuid: UUID) =
    for{
      _ <- menuPerDayPerPersonService.deleteByMenuPerPersonUuid(uuid)
      result <- menuPerDayService.delete(uuid)
    } yield result

  private def getDateStart = new java.sql.Date(new Date().getTime)

  private def getDateEnd = {
    val dateTime = new DateTime(new Date())
    new java.sql.Date(dateTime.plusDays(90).toDate.getTime)
  }

  private def getAllFilterByDateRange(form: FilterMenusPerDayForm): Future[Seq[MenuWithNamePerDay]] =
    menuPerDayPerPersonService.getAllMenuWithNamePerDayFilterDateRange(
      new java.sql.Date(form.dateStart.getTime),
      new java.sql.Date(form.dateEnd.getTime))

}
