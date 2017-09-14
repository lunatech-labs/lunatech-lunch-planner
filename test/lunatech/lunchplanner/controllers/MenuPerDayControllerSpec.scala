package lunatech.lunchplanner.controllers

import java.util
import java.util.UUID

import akka.stream.Materializer
import lunatech.lunchplanner.common.{ControllerSpec, DBConnection}
import lunatech.lunchplanner.data.ControllersData._
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.services._
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.{call, _}
import play.api.{Configuration, Environment}
import slick.lifted.TableQuery

import scala.concurrent.Future

class MenuPerDayControllerSpec extends ControllerSpec {
  implicit lazy val materializer: Materializer = app.materializer

  private val developer = User(UUID.randomUUID(), "Developer", "developer@lunatech.com", isAdmin = true)

  val userService = mock[UserService]
  val userProfileService = mock[UserProfileService]
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

  val uuid = UUID.randomUUID().toString

  when(configuration.getStringList("administrators")).thenReturn(Some(adminList))
  when(userService.getByEmailAddress("developer@lunatech.com")).thenReturn(Future.successful(Some(developer)))
  when(menuPerDayPerPersonService.getAllMenuWithNamePerDayFilterDateRange(any[java.sql.Date], any[java.sql.Date]))
    .thenReturn(Future.successful(Seq(schedule1, schedule2)))
  when(menuService.getAllMenusUuidAndNames).thenReturn(Future.successful(Seq(uuid -> "MyMenu")))

  val controller = new MenuPerDayController(
    userService,
    userProfileService,
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

//      status(result) mustBe 200
//      contentAsString(result).contains("Menu 1") mustBe true
//      contentAsString(result).contains("Menu 2") mustBe true
    }

    "not accept location not in scope" in {
      val result = route(app, FakeRequest(POST, "/menuPerDay/add")
        .withSession("email" -> "developer@lunatech.com")
        .withFormUrlEncodedBody("date" -> "23-04-2017",
          "menuUuid" -> uuid,
          "location" -> "The Hague"))

      result match {
        case Some(s) => contentAsString(s).contains("The Hague is not a valid office location") mustBe true
        case _ => fail("Invalid locations are accepted! This should not happen!")
      }
    }
  }


}
