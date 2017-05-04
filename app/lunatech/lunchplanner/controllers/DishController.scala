package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Dish, Menu, MenuWithDishes, MenuWithNamePerDay }
import lunatech.lunchplanner.persistence.UserTable
import lunatech.lunchplanner.services.DishService
import lunatech.lunchplanner.viewModels.DishForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.Json
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

  def getAllDishes() = IsAdminAsync { username =>
    implicit request =>
    val allDishes = dishService.getAllDishes
    allDishes.map{
      dishes => Ok(Json.toJson(dishes))
    }
  }

  def createNewDish() = IsAdminAsync { username =>
    implicit request => {
      val currentUser = UserTable.getUserByEmailAddress(username)
      currentUser.flatMap(user =>
        DishController
          .dishForm
          .bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(views.html.admin(
              activeTab = 0,
              user.get,
              formWithErrors,
              MenuController.menuForm,
              Array.empty[Dish],
              Array.empty[MenuWithDishes],
              MenuPerDayController.menuPerDayForm,
              Seq.empty[(String, String)],
              Array.empty[MenuWithNamePerDay]))),
            dishData => {
              dishService.addNewDish(dishData).map { _ =>
                Redirect(lunatech.lunchplanner.controllers.routes.Application.admin(activePage = 0))
              }
            }
          )
      )
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

object DishController {
  val dishForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "isVegetarian" -> boolean,
      "hasSeaFood" -> boolean,
      "hasPork" -> boolean,
      "hasBeef" -> boolean,
      "hasChicken" -> boolean,
      "isGlutenFree" -> boolean,
      "hasLactose" -> boolean,
      "remarks" -> optional(text)
  )(DishForm.apply)(DishForm.unapply)
  )
}
