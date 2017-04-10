package lunatech.lunchplanner.controllers

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

  def getAllDishes(activePage: Int) = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
      } yield
        Ok(views.html.admin.dishes(activePage, currentUser.get, DishForm.dishForm, dishes))
    }
  }

  def createNewDish = IsAdminAsync { username =>
    implicit request => {
      for{
        currentUser <- UserTable.getUserByEmailAddress(username)
        dishes <- dishService.getAllDishes.map(_.toArray)
        result <- DishForm
          .dishForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(
              views.html.admin.dishes(activeTab = 0, currentUser.get, formWithErrors, dishes))),
            dishData =>
              dishService.addNewDish(dishData).map( _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.DishController.getAllDishes()))
          )
      } yield result
    }
  }

  def removeDish() = ???
  def filterDishByUUID = ???
  def filterDishByName = ???
  def filterIsvegetarianDishes= ???
  def filterHasSeaFoodDishes = ???
  def filterHasPorkDishes = ???
  def filterHasBeefDishes = ???
  def filterHasChickenDishes = ???
  def filterIsGlutenFreeDishes = ???
  def filterHasLactoseDishes = ???
}
