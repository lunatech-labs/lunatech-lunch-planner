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

class MenuPerDayPerPersonController @Inject() (
  userService: UserService,
  menuPerDayService: MenuPerDayService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection) extends Controller with Secured with I18nSupport {

  def createNewMenuPerDayPerPerson = IsAuthenticatedAsync { username =>
    implicit request => {

      for{
        user <- userService.getUserByEmailAddress(username)
        menusPerDayPerPerson <- menuPerDayPerPersonService.getAllMenuWithNamePerDayWithDishesPerPerson(user.get.uuid).map(_.toArray)
        result <- MenuPerDayPerPersonForm
            .menuPerDayPerPersonForm
            .bindFromRequest
            .fold(
              formWithErrors => Future.successful(
                BadRequest(views.html.index(
                  user.get,
                  isUserAdmin = userService.isAdminUser(user.get.emailAddress),
                  menusPerDayPerPerson,
                  formWithErrors))),
              menuPerDayPerPersonData => {
                updateMenusPerDayPerPerson(user.get.uuid, menuPerDayPerPersonData).map(_ =>
                  Redirect(lunatech.lunchplanner.controllers.routes.Application.index()))
              }
            )
      } yield result
      }
    }

  private def updateMenusPerDayPerPerson(userUuid: UUID, form: MenuPerDayPerPersonForm) = {
    menuPerDayPerPersonService.getAllMenusPerDayPerPersonByUserUuid(userUuid).map(allMenusPerDayPerPerson => {
      menusPerDayToAdd(allMenusPerDayPerPerson, userUuid, form)
      menusPerDayToRemove(allMenusPerDayPerPerson, userUuid, form)
    })
  }

  private def menusPerDayToAdd(menusChosen: Seq[MenuPerDayPerPerson], userUuid: UUID, form: MenuPerDayPerPersonForm) =
    form.menuPerDayUuid
      .filter(!menusChosen.map(_.menuPerDayUuid).contains(_))
      .foreach{ uuid =>
      val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = uuid, userUuid = userUuid)
      menuPerDayPerPersonService.addNewMenusPerDayPerPerson(newMenuPerDayPerPerson)
    }

  private def menusPerDayToRemove(allMenusPerDayPerPerson: Seq[MenuPerDayPerPerson], userUuid: UUID, form: MenuPerDayPerPersonForm) =
    allMenusPerDayPerPerson.filter(menu => !form.menuPerDayUuid.contains(menu.menuPerDayUuid))
      .foreach(menu => menuPerDayPerPersonService.removeMenuPerDayPerPerson(menu.uuid))

}