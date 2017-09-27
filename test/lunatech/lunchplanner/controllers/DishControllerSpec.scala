package lunatech.lunchplanner.controllers

import java.util
import java.util.UUID

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.common.{ControllerSpec, DBConnection}
import lunatech.lunchplanner.data.ControllersData._
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.services.{DishService, MenuDishService, UserService}
import org.mockito.Mockito._
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.{call, status, _}
import play.api.{Configuration, Environment}
import slick.lifted.TableQuery

import scala.concurrent.Future

class DishControllerSpec extends ControllerSpec {
  implicit lazy val materializer: Materializer = app.materializer

  val config = Configuration(ConfigFactory.load())

  private val developer = User(UUID.randomUUID(), "Developer", "developer@lunatech.com")

  val userService = mock[UserService]
  val dishService = mock[DishService]
  val menuDishService = mock[MenuDishService]
  val environment = mock[Environment]
  val messagesApi = new DefaultMessagesApi(Environment.simple(), config, new DefaultLangs(config))
  val configuration = mock[Configuration]
  val connection = mock[DBConnection]

  val dishTable: TableQuery[DishTable] = TableQuery[DishTable]

  val adminList: util.ArrayList[String] = new java.util.ArrayList[String]()
  adminList.add("developer@lunatech.com")

  when(configuration.getStringList("administrators")).thenReturn(Some(adminList))
  when(userService.getByEmailAddress("developer@lunatech.com")).thenReturn(Future.successful(Some(developer)))
  when(dishService.getAll).thenReturn(Future.successful(Seq(dish1, dish2, dish3, dish4, dish5)))

  val controller = new DishController(
    userService,
    dishService,
    menuDishService,
    environment,
    messagesApi,
    configuration,
    connection)

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
