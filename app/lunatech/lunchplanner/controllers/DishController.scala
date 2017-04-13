package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services.{ DishService, UserService }
import lunatech.lunchplanner.viewModels.{ DishForm, ListDishesForm, MenuPerDayPerPersonForm }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DishController @Inject() (
  userService: UserService,
  dishService: DishService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  implicit val connection: DBConnection)
  extends Controller with Secured with I18nSupport {

  def getAllDishes = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        dishes <- dishService.getAll.map(_.toArray)
      } yield
        Ok(views.html.admin.dish.dishes(currentUser.get, ListDishesForm.listDishesForm, dishes))
    }
  }

  def getNewDish = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
      } yield
        Ok(views.html.admin.dish.newDish(currentUser.get, DishForm.dishForm))
    }
  }

  def createNewDish = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        result <- DishForm
          .dishForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(
              views.html.admin.dish.newDish(currentUser.get, formWithErrors))),
            dishData =>
              dishService.add(dishData).map( _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.DishController.getAllDishes()))
          )
      } yield result
    }
  }

  def getDishDetails(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        dish <- dishService.getByUuid(uuid)
      } yield
        Ok(views.html.admin.dish.dishDetails(currentUser.get, DishForm.dishForm, dish))
    }
  }

  def saveDishDetails(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        dish <- dishService.getByUuid(uuid)
        result <- DishForm
          .dishForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(
              views.html.admin.dish.dishDetails(currentUser.get, formWithErrors, dish))),
            dishData =>
              dishService.insertOrUpdate(uuid, dishData).map( _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.DishController.getAllDishes()))
          )
      } yield result
    }
  }

  def deleteDishes = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        dishes <- dishService.getAll.map(_.toArray)
        result <- ListDishesForm
          .listDishesForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(
              views.html.admin.dish.dishes(currentUser.get, ListDishesForm.listDishesForm, dishes))),
            dishData =>
              dishService.deleteMany(dishData).map( _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.DishController.getAllDishes()))
          )
      } yield result
    }
  }

  def deleteDish(uuid: UUID) = IsAdminAsync { username =>
    implicit request => {
      for{
        _ <- dishService.delete(uuid)
        currentUser <- userService.getByEmailAddress(username)
        dishes <- dishService.getAll.map(_.toArray)
      } yield
        Ok(views.html.admin.dish.dishes(currentUser.get, ListDishesForm.listDishesForm, dishes))
    }
  }
}
