package lunatech.lunchplanner.controllers

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuPerDayPerPerson
import lunatech.lunchplanner.services.{ MenuPerDayPerPersonService, MenuPerDayService, UserService }
import lunatech.lunchplanner.viewModels.MenuPerDayPerPersonForm
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{ BaseController, ControllerComponents, EssentialAction }
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonController @Inject()(
  userService: UserService,
  menuPerDayService: MenuPerDayService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  val controllerComponents: ControllerComponents,
  val environment: Environment,
  val configuration: Configuration,
  implicit val connection: DBConnection) extends BaseController with Secured with I18nSupport {

  def createNewMenuPerDayPerPerson: EssentialAction = userAction.async { implicit request =>
      for {
        currentUser <- userService.getByEmailAddress(request.email)
        result <- MenuPerDayPerPersonForm
          .menuPerDayPerPersonForm
          .bindFromRequest
          .fold(
            formWithErrors =>
              for {
                menusPerDayPerPerson <- menuPerDayPerPersonService.getAllMenuWithNamePerDayWithDishesPerPerson(
                  getCurrentUser(currentUser, isAdmin = false, request.email).uuid)
              } yield {
                BadRequest(views.html.menuPerDayPerPerson(
                  getCurrentUser(currentUser, isAdmin = userService.isAdminUser(currentUser.get.emailAddress), request.email),
                  menusPerDayPerPerson.toArray,
                  formWithErrors
                ))
              },
            formData => {
              updateData(getCurrentUser(currentUser, isAdmin = false, request.email).uuid, formData).map { _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.Application.index())
                  .flashing("success" -> "Meals updated!")
              }
            }
          )
      } yield result
  }

  def getAttendeesEmailAddressesForUpcomingLunch: EssentialAction = Action.async {
    implicit request => {
      for {
        emails <- menuPerDayPerPersonService.getAttendeesEmailAddressesForUpcomingLunch
      } yield Ok(Json.toJson(emails))
    }
  }

  private def updateData(userUuid: UUID, form: MenuPerDayPerPersonForm): Future[Boolean] = {
    updateMenusPerDayPerPerson(form.menuPerDayUuids, form.menuPerDayUuidsNotAttending, userUuid)
    Future.successful(true)
  }

  private def updateMenusPerDayPerPerson(menuPerDayUuidList: List[UUID], uuidsNotAttending: List[String], userUuid: UUID) = {
    remove(userUuid)
    addAttending(menuPerDayUuidList, userUuid)
    addNotAttending(uuidsNotAttending, userUuid)
  }

  private def remove(userUuid: UUID) = {
    for {
      allMenuPerDayPerPerson <- menuPerDayPerPersonService.getAllUpcomingSchedulesByUser(userUuid)
    } yield {
      allMenuPerDayPerPerson.foreach { menuPerDayPerPerson  =>
        menuPerDayPerPersonService.delete(menuPerDayPerPerson.uuid)
      }
    }
  }

  private def addAttending(menuPerDayUuidList: List[UUID], userUuid: UUID) = {
    menuPerDayUuidList.foreach { menuPerDayUuid =>
      val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = menuPerDayUuid,
        userUuid = userUuid,
        isAttending = true)
      menuPerDayPerPersonService.add(newMenuPerDayPerPerson)
    }
  }

  private def addNotAttending(uuidsNotAttending: List[String], userUuid: UUID): Unit = {
    uuidsNotAttending.foreach { uuids =>
      uuids.split("~").foreach { uuid =>
        val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = UUID.fromString(uuid),
          userUuid = userUuid,
          isAttending = false)
        menuPerDayPerPersonService.add(newMenuPerDayPerPerson)
      }
    }
  }
}
