package lunatech.lunchplanner.controllers

import java.util.UUID

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.common.{ ControllerSpec, DBConnection }
import lunatech.lunchplanner.data.ControllersData._
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.services._
import org.mockito.Mockito._
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{ call, status, _ }
import play.api.{ Configuration, Environment }
import play.test.WithApplication
import slick.lifted.TableQuery

import scala.concurrent.Future

class MenuControllerSpec extends ControllerSpec {

  implicit lazy private val materializer: Materializer = app.materializer

  private val config = Configuration(ConfigFactory.load)

  private val developer = User(UUID.randomUUID, "Developer", "developer@lunatech.nl")

  private val userService = mock[UserService]
  private val dishService = mock[DishService]
  private val menuService = mock[MenuService]
  private val menuDishService = mock[MenuDishService]
  private val menuPerDayService = mock[MenuPerDayService]
  private val menuPerDayPerPersonService = mock[MenuPerDayPerPersonService]
  private val environment = mock[Environment]
  private val controllerComponents = app.injector.instanceOf[ControllerComponents]
  private val configuration = app.injector.instanceOf[Configuration]
  private val connection = mock[DBConnection]

  private val dishTable = TableQuery[DishTable]

  when(userService.getByEmailAddress("developer@lunatech.nl")).thenReturn(Future.successful(Some(developer)))
  when(menuDishService.getAllWithListOfDishes).thenReturn(Future.successful(Seq(menuDish1, menuDish2)))

  private val controller = new MenuController(
    userService,
    dishService,
    menuService,
    menuPerDayService,
    menuPerDayPerPersonService,
    menuDishService,
    controllerComponents,
    environment,
    configuration)(connection)

  "Menu controller" should {

    "display list of menus" in new WithApplication() {
      val request = FakeRequest().withSession("email" -> "developer@lunatech.nl")
      val result = call(controller.getAllMenus, request)

      status(result) mustBe 200
      contentAsString(result).contains("Menu 1") mustBe true
      contentAsString(result).contains("Menu 2") mustBe true
      contentAsString(result).contains("Prosciutto crudo di Parma e melone") mustBe true
      contentAsString(result).contains("Insalata tricolore") mustBe true
      contentAsString(result).contains("Gamberoni all aglio") mustBe true
    }
  }

}
