package lunatech.lunchplanner.controllers

import java.util
import java.util.UUID

import akka.stream.Materializer
import lunatech.lunchplanner.common.{ ControllerSpec, DBConnection }
import lunatech.lunchplanner.data.ControllersData._
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.services.{ DishService, MenuDishService, MenuPerDayPerPersonService, MenuPerDayService, MenuService, UserService }
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.{ call, status, _ }
import play.api.{ Configuration, Environment }
import slick.lifted.TableQuery

import scala.concurrent.Future

class MenuPerDayControllerSpec extends ControllerSpec {
  implicit lazy val materializer: Materializer = app.materializer

  private val developer = User(UUID.randomUUID(), "Developer", "developer@lunatech.com")

  val userService = mock[UserService]
  val dishService = mock[DishService]
  val menuService = mock[MenuService]
  val menuDishService = mock[MenuDishService]
  val menuPerDayService = mock[MenuPerDayService]
  val menuPerDayPerPersonService = mock[MenuPerDayPerPersonService]
  val environment = mock[Environment]
  val messagesApi = mock[MessagesApi]
  val configuration = mock[Configuration]
  val connection = mock[DBConnection]

  val dishTable: TableQuery[DishTable] = TableQuery[DishTable]

  val adminList: util.ArrayList[String] = new java.util.ArrayList[String]()
  adminList.add("developer@lunatech.com")

  when(configuration.getStringList("administrators")).thenReturn(Some(adminList))
  when(userService.getByEmailAddress("developer@lunatech.com")).thenReturn(Future.successful(Some(developer)))
  when(menuPerDayPerPersonService.getAllMenuWithNamePerDay).thenReturn(Future.successful(Seq(schedule1, schedule2)))

  val controller = new MenuPerDayController(
    userService,
    menuService,
    menuDishService,
    menuPerDayService,
    menuPerDayPerPersonService,
    environment,
    messagesApi,
    configuration,
    connection)

  "Menu per day controller" should {
    "display list of menus per day (schedules)" in {
      val request = FakeRequest().withSession("email" -> "developer@lunatech.com")
      val result = call(controller.getAllMenusPerDay, request)

      status(result) mustBe 200
      contentAsString(result).contains("Menu 1") mustBe true
      contentAsString(result).contains("Menu 2") mustBe true
    }
  }

}