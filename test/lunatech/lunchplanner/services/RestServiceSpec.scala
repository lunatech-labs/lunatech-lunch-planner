package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.BehaviorTestingConfig
import lunatech.lunchplanner.persistence.UserTable
import play.api.{ConfigLoader, Configuration}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import lunatech.lunchplanner.persistence.MenuPerDayPerPersonTable
import lunatech.lunchplanner.models.MenuPerDayPerPerson
import lunatech.lunchplanner.persistence.MenuPerDayTable
import lunatech.lunchplanner.persistence.MenuTable

class RestServiceSpec
    extends BehaviorTestingConfig
    with BeforeAndAfterEach
    with MockFactory {

  import lunatech.lunchplanner.data.ControllersData._

  private val menuService       = mock[MenuService]
  private val dishService       = mock[DishService]
  private val menuPerDayService = new MenuPerDayService(menuService)
  private val menuPerDayPerPersonService =
    new MenuPerDayPerPersonService(dishService, menuService, menuPerDayService)
  private val menuDishService = new MenuDishService(dishService, menuService)

  private val configuration = mock[Configuration]
  private val restService = new RestService(
    menuService = menuService,
    menuPerDayService = menuPerDayService,
    menuPerDayPerPersonService = menuPerDayPerPersonService,
    menuDishService = menuDishService
  )

  override def beforeEach(): Unit = {
    createTestSchema()
    Await.result(
      for {
        _ <- UserTable.add(user1)
        _ <- UserTable.add(user2)
        _ <- MenuTable.add(event1Menu)
        _ <- MenuPerDayTable.add(event1MenuPerDay)
        _ <- MenuPerDayPerPersonTable.addOrUpdate(userAttending)
      } yield (),
      defaultTimeout
    )
  }

  override def afterEach(): Unit = dropTestSchema()

  "rest service" should {
    "register user as attending" in {
      val result = Await
        .result(
          restService
            .registerAttendance(event1MenuPerDayUuid, user2.uuid, true),
          defaultTimeout
        )

      result.menuPerDayUuid mustBe event1MenuPerDayUuid
      result.userUuid mustBe user2.uuid
      result.isAttending mustBe true
    }

    "update user as attending" in {
      val result = Await
        .result(
          restService
            .registerAttendance(event1MenuPerDayUuid, user1.uuid, true),
          defaultTimeout
        )

      result.menuPerDayUuid mustBe event1MenuPerDayUuid
      result.userUuid mustBe user1.uuid
      result.isAttending mustBe true
    }

    "update user as not attending" in {
      val result = Await
        .result(
          restService
            .registerAttendance(event1MenuPerDayUuid, user1.uuid, false),
          defaultTimeout
        )

      result.menuPerDayUuid mustBe event1MenuPerDayUuid
      result.userUuid mustBe user1.uuid
      result.isAttending mustBe false
    }
  }
}
