package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.services.{ MenuPerDayPerPersonService, UserService }
import lunatech.lunchplanner.viewModels.MenuPerDayPerPersonForm
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Controller, Flash }
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (
  userService: UserService,
  menuPerDayPerPersonService: MenuPerDayPerPersonService,
  val connection: DBConnection,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration)
  extends Controller with Secured with I18nSupport {

  def index = IsAuthenticatedAsync { username =>
    implicit request =>
      userService.getByEmailAddress(username).flatMap(currentUser =>
        getIndex(currentUser))
  }

  private def getIndex(normalUser: Option[User])(implicit flash: Flash) =
    normalUser match {
      case Some(user) => getMenuPerDayPerPerson(user)
      case None => Future.successful(Unauthorized)
    }

  private def getMenuPerDayPerPerson(user: User)(implicit flash: Flash) = {
    val userIsAdmin = userService.isAdminUser(user.emailAddress)
    menuPerDayPerPersonService.getAllMenuWithNamePerDayWithDishesPerPerson(user.uuid).map(_.toArray)
      .map(menusPerDayPerPerson =>
        Ok(views.html.menuPerDayPerPerson(
          user.copy(isAdmin = userIsAdmin),
          menusPerDayPerPerson,
          MenuPerDayPerPersonForm.menuPerDayPerPersonForm)))
  }
}
