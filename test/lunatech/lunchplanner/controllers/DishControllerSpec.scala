package lunatech.lunchplanner.controllers

import java.util.UUID

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.common.{ ControllerSpec, DBConnection }
import lunatech.lunchplanner.data.ControllersData._
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.services.{ DishService, MenuDishService, UserService }
import org.mockito.Mockito._
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{ call, status, _ }
import play.api.{ Configuration, Environment }
import slick.lifted.TableQuery

import scala.concurrent.Future

class DishControllerSpec extends ControllerSpec {
  implicit lazy private val materializer: Materializer = app.materializer

  private val config = Configuration(ConfigFactory.load())

  private val developer = User(UUID.randomUUID(), "Developer", "developer@lunatech.com")

  private val userService = mock[UserService]
  private val dishService = mock[DishService]
  private val menuDishService = mock[MenuDishService]
  private val environment = mock[Environment]
  private val controllerComponents = app.injector.instanceOf[ControllerComponents]
  private val configuration = app.injector.instanceOf[Configuration]
  private val connection = mock[DBConnection]

  private val dishTable = TableQuery[DishTable]

  when(userService.getByEmailAddress("developer@lunatech.com")).thenReturn(Future.successful(Some(developer)))
  when(dishService.getAll).thenReturn(Future.successful(Seq(dish1, dish2, dish3, dish4, dish5)))

  private val controller = new DishController(
    userService,
    dishService,
    menuDishService,
    controllerComponents,
    environment,
    configuration)(connection)

  "Dish controller" should {

    "display list of dishes" in {
      val request = FakeRequest().withSession("email" -> "developer@lunatech.com")
      val result = call(controller.getAllDishes, request)

      status(result) mustBe 200
      contentAsString(result).contains("Antipasto misto all italiana") mustBe true
      contentAsString(result).contains("Prosciutto crudo di Parma e melone") mustBe true
      contentAsString(result).contains("Insalata tricolore") mustBe true
      contentAsString(result).contains("Avocado al forno") mustBe true
      contentAsString(result).contains("Gamberoni all aglio") mustBe true
    }
  }

}
