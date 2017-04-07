package lunatech.lunchplanner.controllers

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDayPerPerson, MenuWithNamePerDayPerPerson }
import lunatech.lunchplanner.services.{ MenuPerDayPerPersonService, MenuPerDayService, UserService }
import lunatech.lunchplanner.viewModels.MenuPerDayPerPersonForm
import play.api.data.Form
import play.api.data.Forms.{ list, mapping, of, _ }
import play.api.data.format.Formats._
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
        menusPerDayPerPerson <- menuPerDayPerPersonService.getAllMenuWithNamePerDayPerPerson(user.get.uuid).map(_.toArray)
        result <- MenuPerDayPerPersonController
            .menuPerDayPerPersonForm
            .bindFromRequest
            .fold(
              formWithErrors => Future.successful(
                BadRequest(views.html.index(user.get, menusPerDayPerPerson, formWithErrors))),
              menuPerDayPerPersonData => {
                updateMenusPerDayPerPerson(user.get.uuid, menuPerDayPerPersonData).map(_ =>
                  Redirect(lunatech.lunchplanner.controllers.routes.Application.index()))
              }
            )
      } yield result
      }
    }

  private def updateMenusPerDayPerPerson(userUuid: UUID, form: MenuPerDayPerPersonForm) = {
    val allMenusPerDayPerPerson = menuPerDayPerPersonService.getAllMenusPerDayPerPersonByUserUuid(userUuid)
    menusPerDayToAdd(allMenusPerDayPerPerson, userUuid, form)
    menusPerDayToRemove(allMenusPerDayPerPerson, userUuid, form)
  }

  private def menusPerDayToAdd(allMenusPerDayPerPerson: Future[Seq[MenuPerDayPerPerson]], userUuid: UUID, form: MenuPerDayPerPersonForm) =
    allMenusPerDayPerPerson.map{ menusChosen =>
      form.menuPerDayUuid
        .filter(!menusChosen.map(_.menuPerDayUuid).contains(_))
        .foreach{ uuid =>
        val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = uuid, userUuid = userUuid)
        menuPerDayPerPersonService.addNewMenusPerDayPerPerson(newMenuPerDayPerPerson)
      }
    }

  private def menusPerDayToRemove(allMenusPerDayPerPerson: Future[Seq[MenuPerDayPerPerson]], userUuid: UUID, form: MenuPerDayPerPersonForm) =
    allMenusPerDayPerPerson.map(_.filter(menu => !form.menuPerDayUuid.contains(menu.menuPerDayUuid))
      .foreach(menu => menuPerDayPerPersonService.removeMenuPerDayPerPerson(menu.uuid)))

}

object MenuPerDayPerPersonController {
  val menuPerDayPerPersonForm = Form(
    mapping(
      "menuPerDayUuid" -> list(of[UUID])
    )(MenuPerDayPerPersonForm.apply)(MenuPerDayPerPersonForm.unapply)
  )
}
