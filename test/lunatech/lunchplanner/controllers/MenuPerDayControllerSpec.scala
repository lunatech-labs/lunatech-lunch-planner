package lunatech.lunchplanner.controllers

import java.util.UUID

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.common.{ ControllerSpec, DBConnection }
import lunatech.lunchplanner.data.ControllersData._
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.services._
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{ Configuration, Environment }
import slick.lifted.TableQuery
import play.api.mvc.ControllerComponents

import scala.concurrent.Future

class MenuPerDayControllerSpec extends ControllerSpec {

  implicit lazy private val materializer: Materializer = app.materializer

  private val config = Configuration(ConfigFactory.load())

  private val developer = User(UUID.randomUUID(), "Developer", "developer@lunatech.com", isAdmin = true)

  private val userService = mock[UserService]
  private val userProfileService = mock[UserProfileService]
  private val menuService = mock[MenuService]
  private val menuDishService = mock[MenuDishService]
  private val menuPerDayService = mock[MenuPerDayService]
  private val menuPerDayPerPersonService = mock[MenuPerDayPerPersonService]
  private val environment = mock[Environment]
  private val controllerComponents = app.injector.instanceOf[ControllerComponents]
  private val configuration = app.injector.instanceOf[Configuration]
  private val connection = mock[DBConnection]

  val dishTable = TableQuery[DishTable]

  val uuid = UUID.randomUUID.toString

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
    controllerComponents,
    environment,
    configuration)(connection)

  "Menu per day controller" should {
    "display list of menus per day (schedules)" in {
      val request = FakeRequest().withSession("email" -> "developer@lunatech.com")
      val result = call(controller.getAllMenusPerDay, request)

      status(result) mustBe 200
      contentAsString(result).contains("Menu 1") mustBe true
      contentAsString(result).contains("Menu 2") mustBe true
    }

    "not accept location not in scope" in {
      val result = route(app, FakeRequest(POST, "/menuPerDay/add")
        .withSession("email" -> "developer@lunatech.com")
        .withFormUrlEncodedBody("date" -> "23-04-2017",
          "menuUuid" -> uuid,
          "location" -> "The Hague"))

      result match {
        case Some(r) =>
          status(r) mustBe 400
          contentAsString(r).contains("The Hague is not a valid office location") mustBe true
        case _ => fail("Invalid locations were accepted! This should not happen!")
      }
    }
  }


}
