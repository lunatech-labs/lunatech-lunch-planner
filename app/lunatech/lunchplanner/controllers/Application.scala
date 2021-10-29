package lunatech.lunchplanner.controllers

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.services.{MenuPerDayPerPersonService, UserService}
import lunatech.lunchplanner.viewModels.MenuPerDayPerPersonForm
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{
  Action,
  AnyContent,
  BaseController,
  ControllerComponents,
  Flash
}
import play.api.{Configuration, Environment}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (
    userService: UserService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    val controllerComponents: ControllerComponents,
    val connection: DBConnection,
    val environment: Environment,
    val configuration: Configuration
) extends BaseController
    with Secured
    with I18nSupport {

  def index: Action[AnyContent] = userAction.async { implicit request =>
    userService
      .getByEmailAddress(request.email)
      .flatMap(currentUser => getIndex(currentUser))
  }

  private def getIndex(
      normalUser: Option[User]
  )(implicit messagesApi: Messages, flash: Flash) =
    normalUser match {
      case Some(user) => getMenuPerDayPerPerson(user)
      case None       => Future.successful(Unauthorized)
    }

  private def getMenuPerDayPerPerson(
      user: User
  )(implicit messagesApi: Messages, flash: Flash) = {
    val userIsAdmin = userService.isAdminUser(user.emailAddress)
    for {
      menusPerDayPerPerson <- menuPerDayPerPersonService
        .getAllMenuWithNamePerDayWithDishesPerPerson(user.uuid)
        .map(_.toArray)
    } yield Ok(
      views.html.menuPerDayPerPerson(
        user.copy(isAdmin = userIsAdmin),
        menusPerDayPerPerson,
        MenuPerDayPerPersonForm.menuPerDayPerPersonForm
      )
    )
  }
}
