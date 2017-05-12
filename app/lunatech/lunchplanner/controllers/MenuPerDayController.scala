package lunatech.lunchplanner.controllers

import java.text.SimpleDateFormat
import java.util.{ Date, UUID }

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuPerDay
import lunatech.lunchplanner.services.{ MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserProfileService, UserService }
import lunatech.lunchplanner.viewModels.{ FilterMenusPerDayForm, ListMenusPerDayForm, MenuPerDayForm }
import org.joda.time.DateTime
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayController @Inject() (
  userService: UserService,
  userProfileService: UserProfileService,
  menuService: MenuService,
  menuDishService: MenuDishService,
  menuPerDayService: MenuPerDayService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  val DateStart = "dateStart"
  val DateEnd = "dateEnd"

  def getAllMenusPerDay =
    IsAdminAsync { username =>
      implicit request => {
        val dStart = request.session.get(DateStart).map(java.sql.Date.valueOf).getOrElse(getDateStart)
        val dEnd = request.session.get(DateEnd).map(java.sql.Date.valueOf).getOrElse(getDateEnd)

        for{
          currentUser <- userService.getByEmailAddress(username)
          menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDayFilterDateRange(dStart, dEnd)
              .map(_.toArray)
        } yield
          Ok(views.html.admin.menuPerDay.menusPerDay(
            getCurrentUser(currentUser, isAdmin = true, username),
            new SimpleDateFormat("dd-MM-yyyy").format(dStart),
            new SimpleDateFormat("dd-MM-yyyy").format(dEnd),
            ListMenusPerDayForm.listMenusPerDayForm,
            menusPerDay))
      }
  }

  def filterMenusPerDay = IsAdminAsync { _ =>
    implicit request => {
      FilterMenusPerDayForm
        .filterMenusPerDayForm
        .bindFromRequest
        .fold(
          _ => {
            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay()))
          },
          filterDataForm => {
            val start = new SimpleDateFormat("yyyy-MM-dd").format(filterDataForm.dateStart)
            val end = new SimpleDateFormat("yyyy-MM-dd").format(filterDataForm.dateEnd)
            val session = request.session + (DateStart -> start) + (DateEnd -> end)

            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay())
              .withSession(session))
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
              currentUser <- userService.getByEmailAddress(username)
              menusUuidAndNames <- menuService.getAllMenusUuidAndNames
            } yield BadRequest(views.html.admin.menuPerDay.newMenuPerDay(
              getCurrentUser(currentUser, isAdmin = true, username),
              currentDate,
              formWithErrors,
              menusUuidAndNames))},
          menuPerDayForm => {
            menuPerDayService.add(getNewMenuPerDay(menuPerDayForm)).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay())
                .flashing("success" -> "New schedule created!"))
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
        Ok(views.html.admin.menuPerDay.newMenuPerDay(
          getCurrentUser(currentUser, isAdmin = true, username),
          currentDate,
          MenuPerDayForm.menuPerDayForm,
          menusUuidAndNames))
    }
  }

  def deleteMenusPerDay() = IsAdminAsync { username =>
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
                getCurrentUser(currentUser, isAdmin = true, username),
                new SimpleDateFormat("dd-MM-yyyy").format(getDateStart),
                new SimpleDateFormat("dd-MM-yyyy").format(getDateEnd),
                formWithErrors,
                menusPerDay))},
          menusPerDayData =>
            deleteSeveral(menusPerDayData).map( _ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay())
                .flashing("success" -> "Schedule(s) deleted!"))
        )
    }
 }

  def getMenuPerDayDetails(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
        menuPerDayOption <- menuPerDayService.getMenuPerDayByUuid(uuid)
        dietRestrictions <- userProfileService.getRestrictionsByMenuPerDay(uuid)
        peopleAttending <- menuPerDayPerPersonService.getListOfPeopleByMenuPerDay(uuid)
      } yield
        Ok(views.html.admin.menuPerDay.menuPerDayDetails(
          getCurrentUser(currentUser, isAdmin = true, username),
          MenuPerDayForm.menuPerDayForm,
          menusUuidAndNames,
          menuPerDayOption,
          dietRestrictions,
          peopleAttending
        ))
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
              dietRestrictions <- userProfileService.getRestrictionsByMenuPerDay(uuid)
              peopleAttendig <- menuPerDayPerPersonService.getListOfPeopleByMenuPerDay(uuid)
            } yield BadRequest(views.html.admin.menuPerDay.menuPerDayDetails(
              getCurrentUser(currentUser, isAdmin = true, username),
              formWithErrors,
              menusUuidAndNames,
              menuPerDayOption,
              dietRestrictions,
              peopleAttendig))},
          _ => {
            delete(uuid).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay())
                .flashing("success" -> "Schedule deleted!"))
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
              dietRestrictions <- userProfileService.getRestrictionsByMenuPerDay(uuid)
              peopleAttendig <- menuPerDayPerPersonService.getListOfPeopleByMenuPerDay(uuid)
            } yield BadRequest(views.html.admin.menuPerDay.menuPerDayDetails(
              getCurrentUser(currentUser, isAdmin = true, username),
              formWithErrors,
              menusUuidAndNames,
              menuPerDayOption,
              dietRestrictions,
              peopleAttendig))},
          menuPerDayData => {
            menuPerDayService.insertOrUpdate(uuid, getNewMenuPerDay(menuPerDayData)).map(_ =>
              Redirect(lunatech.lunchplanner.controllers.routes.MenuPerDayController.getAllMenusPerDay())
                .flashing("success" -> "Schedule updated!"))
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

  private def getNewMenuPerDay(menuPerDayForm: MenuPerDayForm) =
    MenuPerDay(menuUuid = menuPerDayForm.menuUuid, date = new java.sql.Date(menuPerDayForm.date.getTime))
}
