package lunatech.lunchplanner.controllers

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.data.Location
import lunatech.lunchplanner.models.MenuPerDay
import lunatech.lunchplanner.services._
import lunatech.lunchplanner.viewModels.{
  FilterMenusPerDayForm,
  ListMenusPerDayForm,
  MenuPerDayForm
}
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}
import play.api.{Configuration, Environment}

import java.text.SimpleDateFormat
import java.time.{LocalDateTime, ZoneOffset}
import java.util.{Date, UUID}
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayController @Inject() (
    userService: UserService,
    userProfileService: UserProfileService,
    menuService: MenuService,
    menuPerDayService: MenuPerDayService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    val controllerComponents: ControllerComponents,
    val environment: Environment,
    val configuration: Configuration
) extends BaseController
    with Secured
    with I18nSupport {

  val DateStart = "dateStart"
  val DateEnd   = "dateEnd"

  def getAllMenusPerDay: EssentialAction = adminAction.async {
    implicit request =>
      val dStart = request.session
        .get(DateStart)
        .map(java.sql.Date.valueOf)
        .getOrElse(getDateStart)
      val dEnd = request.session
        .get(DateEnd)
        .map(java.sql.Date.valueOf)
        .getOrElse(getDateEnd)

      for {
        currentUser <- userService.getByEmailAddress(request.email)
        menusPerDay <- menuPerDayPerPersonService
          .getAllMenuWithNamePerDayFilterDateRange(dStart, dEnd)
          .map(_.toArray)
      } yield Ok(
        views.html.admin.menuPerDay.menusPerDay(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          new SimpleDateFormat("dd-MM-yyyy").format(dStart),
          new SimpleDateFormat("dd-MM-yyyy").format(dEnd),
          ListMenusPerDayForm.listMenusPerDayForm,
          menusPerDay
        )
      )
  }

  def filterMenusPerDay: EssentialAction = adminAction.async {
    implicit request =>
      FilterMenusPerDayForm.filterMenusPerDayForm
        .bindFromRequest()
        .fold(
          _ =>
            Future.successful(
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuPerDayController
                  .getAllMenusPerDay()
              )
            ),
          filterDataForm => {
            val start = new SimpleDateFormat("yyyy-MM-dd")
              .format(filterDataForm.dateStart)
            val end =
              new SimpleDateFormat("yyyy-MM-dd").format(filterDataForm.dateEnd)
            val session =
              request.session + (DateStart -> start) + (DateEnd -> end)

            Future.successful(
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuPerDayController
                  .getAllMenusPerDay()
              )
                .withSession(session)
            )
          }
        )
  }

  def createNewMenuPerDay: EssentialAction = adminAction.async {
    implicit request =>
      val currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date())
      MenuPerDayForm.menuPerDayForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            for {
              currentUser       <- userService.getByEmailAddress(request.email)
              menusUuidAndNames <- menuService.getAllMenusUuidAndNames
            } yield BadRequest(
              views.html.admin.menuPerDay.newMenuPerDay(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                currentDate,
                formWithErrors,
                menusUuidAndNames,
                Location.values
              )
            ),
          menuPerDayForm =>
            menuPerDayService
              .add(getNewMenuPerDay(menuPerDayForm))
              .map(_ =>
                Redirect(
                  lunatech.lunchplanner.controllers.routes.MenuPerDayController
                    .getAllMenusPerDay()
                )
                  .flashing("success" -> "New schedule created!")
              )
        )
  }

  def getNewMenuPerDay: EssentialAction = adminAction.async {
    implicit request =>
      val currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date())
      for {
        currentUser       <- userService.getByEmailAddress(request.email)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
      } yield Ok(
        views.html.admin.menuPerDay.newMenuPerDay(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          currentDate,
          MenuPerDayForm.menuPerDayForm,
          menusUuidAndNames,
          Location.values
        )
      )
  }

  def deleteMenusPerDay(): EssentialAction = adminAction.async {
    implicit request =>
      ListMenusPerDayForm.listMenusPerDayForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            for {
              currentUser <- userService.getByEmailAddress(request.email)
              menusPerDay <- menuPerDayPerPersonService.getAllMenuWithNamePerDay
                .map(_.toArray)
            } yield BadRequest(
              views.html.admin.menuPerDay.menusPerDay(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                new SimpleDateFormat("dd-MM-yyyy").format(getDateStart),
                new SimpleDateFormat("dd-MM-yyyy").format(getDateEnd),
                formWithErrors,
                menusPerDay
              )
            ),
          menusPerDayData =>
            deleteSeveral(menusPerDayData).map(_ =>
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuPerDayController
                  .getAllMenusPerDay()
              )
                .flashing("success" -> "Schedule(s) deleted!")
            )
        )
  }

  def getMenuPerDayDetails(uuid: UUID): EssentialAction = adminAction.async {
    implicit request =>
      for {
        currentUser       <- userService.getByEmailAddress(request.email)
        menusUuidAndNames <- menuService.getAllMenusUuidAndNames
        menuPerDayOption  <- menuPerDayService.getMenuPerDayByUuid(uuid)
        dietRestrictions <- userProfileService.getRestrictionsByMenuPerDay(uuid)
        peopleAttending <- menuPerDayPerPersonService
          .getListOfPeopleByMenuPerDay(uuid)
      } yield Ok(
        views.html.admin.menuPerDay.menuPerDayDetails(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          MenuPerDayForm.menuPerDayForm,
          menusUuidAndNames,
          menuPerDayOption,
          dietRestrictions,
          peopleAttending,
          Location.values
        )
      )
  }

  def deleteMenuPerDay(uuid: UUID): EssentialAction = adminAction.async {
    implicit request =>
      MenuPerDayForm.menuPerDayForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            for {
              currentUser       <- userService.getByEmailAddress(request.email)
              menusUuidAndNames <- menuService.getAllMenusUuidAndNames
              menuPerDayOption  <- menuPerDayService.getMenuPerDayByUuid(uuid)
              dietRestrictions <- userProfileService
                .getRestrictionsByMenuPerDay(uuid)
              peopleAttending <- menuPerDayPerPersonService
                .getListOfPeopleByMenuPerDay(uuid)
            } yield BadRequest(
              views.html.admin.menuPerDay.menuPerDayDetails(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                formWithErrors,
                menusUuidAndNames,
                menuPerDayOption,
                dietRestrictions,
                peopleAttending,
                Location.values
              )
            ),
          _ =>
            delete(uuid).map(_ =>
              Redirect(
                lunatech.lunchplanner.controllers.routes.MenuPerDayController
                  .getAllMenusPerDay()
              )
                .flashing("success" -> "Schedule deleted!")
            )
        )
  }

  def saveMenuPerDayDetails(uuid: UUID): EssentialAction = adminAction.async {
    implicit request =>
      MenuPerDayForm.menuPerDayForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            for {
              currentUser       <- userService.getByEmailAddress(request.email)
              menusUuidAndNames <- menuService.getAllMenusUuidAndNames
              menuPerDayOption  <- menuPerDayService.getMenuPerDayByUuid(uuid)
              dietRestrictions <- userProfileService
                .getRestrictionsByMenuPerDay(uuid)
              peopleAttending <- menuPerDayPerPersonService
                .getListOfPeopleByMenuPerDay(uuid)
            } yield BadRequest(
              views.html.admin.menuPerDay.menuPerDayDetails(
                getCurrentUser(currentUser, isAdmin = true, request.email),
                formWithErrors,
                menusUuidAndNames,
                menuPerDayOption,
                dietRestrictions,
                peopleAttending,
                Location.values
              )
            ),
          menuPerDayData =>
            menuPerDayService
              .update(uuid, getNewMenuPerDay(menuPerDayData))
              .map(_ =>
                Redirect(
                  lunatech.lunchplanner.controllers.routes.MenuPerDayController
                    .getAllMenusPerDay()
                )
                  .flashing("success" -> "Schedule updated!")
              )
        )
  }

  private def deleteSeveral(form: ListMenusPerDayForm) =
    Future.sequence(form.listUuids.map(uuid => delete(uuid)))

  private def delete(uuid: UUID) =
    for {
      _      <- menuPerDayPerPersonService.deleteByMenuPerPersonUuid(uuid)
      result <- menuPerDayService.delete(uuid)
    } yield result

  private def getDateStart = new java.sql.Date(new Date().getTime)

  private def getDateEnd = {
    val futureDate =
      LocalDateTime.now().plusDays(90).toInstant(ZoneOffset.UTC).toEpochMilli
    new java.sql.Date(futureDate)
  }

  private def getNewMenuPerDay(menuPerDayForm: MenuPerDayForm) =
    MenuPerDay(
      menuUuid = menuPerDayForm.menuUuid,
      date = new java.sql.Date(menuPerDayForm.date.getTime),
      location = menuPerDayForm.location
    )
}
