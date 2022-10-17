package lunatech.lunchplanner.controllers

import lunatech.lunchplanner.models.MenuPerDayPerPerson
import lunatech.lunchplanner.services.{MenuPerDayPerPersonService, UserService}
import lunatech.lunchplanner.viewModels.MenuPerDayPerPersonForm
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}
import play.api.{Configuration, Environment}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class MenuPerDayPerPersonController @Inject() (
    userService: UserService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    val controllerComponents: ControllerComponents,
    val environment: Environment,
    val configuration: Configuration
) extends BaseController
    with Secured
    with I18nSupport {

  def createNewMenuPerDayPerPerson: EssentialAction = userAction.async {
    implicit request =>
      for {
        currentUser <- userService.getByEmailAddress(request.email)
        result <- MenuPerDayPerPersonForm.menuPerDayPerPersonForm
          .bindFromRequest()
          .fold(
            formWithErrors =>
              for {
                menusPerDayPerPerson <- menuPerDayPerPersonService
                  .getAllMenuWithNamePerDayWithDishesPerPerson(
                    getCurrentUser(
                      currentUser,
                      isAdmin = false,
                      request.email
                    ).uuid
                  )
              } yield BadRequest(
                views.html.menuPerDayPerPerson(
                  getCurrentUser(
                    currentUser,
                    isAdmin =
                      userService.isAdminUser(currentUser.get.emailAddress),
                    request.email
                  ),
                  menusPerDayPerPerson.toArray,
                  formWithErrors
                )
              ),
            formData =>
              updateData(
                getCurrentUser(
                  currentUser,
                  isAdmin = false,
                  request.email
                ).uuid,
                formData
              ).map { _ =>
                Redirect(
                  lunatech.lunchplanner.controllers.routes.Application.index()
                )
                  .flashing("success" -> "Meals updated!")
              }
          )
      } yield result
  }

  def getAttendeesEmailAddressesForUpcomingLunch: EssentialAction =
    Action.async { implicit request =>
      for {
        emails <-
          menuPerDayPerPersonService.getAttendeesEmailAddressesForUpcomingLunch
      } yield Ok(Json.toJson(emails))
    }

  private def updateData(
      userUuid: UUID,
      form: MenuPerDayPerPersonForm
  ): Future[Boolean] =
    for {
      _ <- remove(userUuid)
      _ <- addAttending(form.menuPerDayUuids, userUuid)
      _ <- addNotAttending(form.menuPerDayUuidsNotAttending, userUuid)
    } yield true

  private def remove(userUuid: UUID) =
    for {
      allMenuPerDayPerPerson <- menuPerDayPerPersonService
        .getAllUpcomingSchedulesByUser(userUuid)
      _ <- menuPerDayPerPersonService.delete(allMenuPerDayPerPerson.map(_.uuid))
    } yield true

  private def addAttending(
      menuPerDayUuidList: List[UUID],
      userUuid: UUID
  ): Future[List[MenuPerDayPerPerson]] =
    Future.sequence {
      for {
        menuPerDayUuid <- menuPerDayUuidList
      } yield {
        val newMenuPerDayPerPerson = MenuPerDayPerPerson(
          menuPerDayUuid = menuPerDayUuid,
          userUuid = userUuid,
          isAttending = true
        )
        menuPerDayPerPersonService.addOrUpdate(newMenuPerDayPerPerson)
      }
    }

  private def addNotAttending(
      uuidsNotAttending: List[String],
      userUuid: UUID
  ): Future[List[MenuPerDayPerPerson]] =
    Future.sequence {
      for {
        uuids <- uuidsNotAttending
        uuid  <- uuids.split("~")
      } yield {
        val newMenuPerDayPerPerson =
          MenuPerDayPerPerson(
            menuPerDayUuid = UUID.fromString(uuid),
            userUuid = userUuid,
            isAttending = false
          )
        menuPerDayPerPersonService.addOrUpdate(newMenuPerDayPerPerson)
      }
    }
}
