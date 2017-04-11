package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Dish, MenuWithDishes, MenuWithNamePerDay }
import lunatech.lunchplanner.persistence.UserTable
import lunatech.lunchplanner.services.DishService
import lunatech.lunchplanner.viewModels.{ DishForm, MenuForm, MenuPerDayForm }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DishController @Inject() (
  dishService: DishService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def getAllDishes = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
      } yield
        Ok(views.html.admin.dishes(currentUser.get, dishes))
    }
  }

  def getNewDish = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
      } yield
        Ok(views.html.admin.newDish(currentUser.get, DishForm.dishForm))
    }
  }


  def createNewDish = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
        result <- DishForm
          .dishForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(
              views.html.admin.newDish(currentUser.get, formWithErrors))),
            dishData =>
              dishService.addNewDish(dishData).map( _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.DishController.getAllDishes()))
          )
      } yield result
    }
  }

  def getDishDetails(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
        dish <- dishService.getDishByUuid(uuid)
      } yield
        Ok(views.html.admin.dishDetail(currentUser.get, DishForm.dishForm, dish))
    }
  }

  def saveDishDetails(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
        dish <- dishService.getDishByUuid(uuid)
        result <- DishForm
          .dishForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(
              views.html.admin.dishDetail(currentUser.get, formWithErrors, dish))),
            dishData =>
              dishService.insertOrUpdateDish(uuid, dishData).map( _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.DishController.getAllDishes()))
          )
      } yield result
    }
  }
}
