package lunatech.lunchplanner.controllers

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuPerDayPerPerson
import lunatech.lunchplanner.services.{ MenuPerDayPerPersonService, MenuPerDayService, UserService }
import lunatech.lunchplanner.viewModels.MenuPerDayPerPersonForm
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonController @Inject()(
                                               userService: UserService,
                                               menuPerDayService: MenuPerDayService,
                                               menuPerDayPerPersonService: MenuPerDayPerPersonService,
                                               val environment: Environment,
                                               val messagesApi: MessagesApi,
                                               val configuration: Configuration,
                                               implicit val connection: DBConnection) extends Controller with Secured with I18nSupport {

  def createNewMenuPerDayPerPerson = IsAuthenticatedAsync { username =>
    implicit request => {
      for {
        currentUser <- userService.getByEmailAddress(username)
        result <- MenuPerDayPerPersonForm
          .menuPerDayPerPersonForm
          .bindFromRequest
          .fold(
            formWithErrors =>
              menuPerDayPerPersonService.getAllMenuWithNamePerDayWithDishesPerPerson(
                getCurrentUser(currentUser, isAdmin = false, username).uuid).map(menusPerDayPerPerson =>
                  BadRequest(views.html.menuPerDayPerPerson(
                    getCurrentUser(currentUser, isAdmin = userService.isAdminUser(currentUser.get.emailAddress), username),
                    menusPerDayPerPerson.toArray,
                    formWithErrors
                  ))
                ),
            menuPerDayPerPersonData => {
              if (thereAreNoDuplicatedDates(menuPerDayPerPersonData)) {
                updateMenusPerDayPerPerson(getCurrentUser(currentUser, isAdmin = false, username).uuid,
                                           menuPerDayPerPersonData).map { _ =>
                  Redirect(lunatech.lunchplanner.controllers.routes.Application.index())
                    .flashing("success" -> "Meals updated!")
                }
              } else {
                val dates = duplicatedDates(menuPerDayPerPersonData)
                Future.successful(Redirect(lunatech.lunchplanner.controllers.routes.Application.index())
                  .flashing("error" -> s"Error: More than one menu for date(s) $dates was selected!"))
              }
            }
          )
      } yield result
    }
  }

  private def thereAreNoDuplicatedDates(menuPerDayPerPersonForm: MenuPerDayPerPersonForm): Boolean =
    menuPerDayPerPersonForm.menuDate.length == menuPerDayPerPersonForm.menuDate.to[Set].size

  private def duplicatedDates(menuPerDayPerPersonForm: MenuPerDayPerPersonForm): String = {
    val allDates = menuPerDayPerPersonForm.menuDate
    val filteredDates = menuPerDayPerPersonForm.menuDate.to[Set].toList
    val difference = allDates.diff(filteredDates).to[Set]

    difference.mkString(", ")
  }

  private def updateMenusPerDayPerPerson(userUuid: UUID, form: MenuPerDayPerPersonForm): Future[List[MenuPerDayPerPerson]] = {
    menuPerDayPerPersonService.getAllUpcomingSchedulesByUser(userUuid).flatMap(allMenusPerDayPerPerson => {
      menusPerDayToRemove(allMenusPerDayPerPerson, form).flatMap { _ =>
        menusPerDayToAdd(userUuid, form)
      }
    })
  }

  private def menusPerDayToAdd(userUuid: UUID, form: MenuPerDayPerPersonForm) = {
    addMenusPerDayPerPerson(form.menuPerDayUuid, userUuid, isAttending = true)
    addMenusPerDayPerPerson(form.menuPerDayUuidNotAttending, userUuid, isAttending = false)
  }

  private def addMenusPerDayPerPerson(menuPerDayUuidList: List[UUID], userUuid: UUID, isAttending: Boolean) = {
    Future.sequence(
      menuPerDayUuidList.map{ uuid =>
        val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = uuid, userUuid = userUuid, isAttending = isAttending)
        menuPerDayPerPersonService.add(newMenuPerDayPerPerson)
      }
    )
  }

  private def menusPerDayToRemove(menusPerDayPerPerson: Seq[MenuPerDayPerPerson], form: MenuPerDayPerPersonForm): Future[Seq[Int]] = {
    Future.sequence(
      menusPerDayPerPerson.filter(menuPerDayPerPerson =>
        !form.menuPerDayUuid.contains(menuPerDayPerPerson.menuPerDayUuid) || !form.menuPerDayUuidNotAttending.contains(menuPerDayPerPerson.menuPerDayUuid))
        .map(menu => menuPerDayPerPersonService.delete(menu.uuid))
    )
  }
}
