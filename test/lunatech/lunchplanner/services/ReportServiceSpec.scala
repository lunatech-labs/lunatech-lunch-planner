package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.BehaviorTestingConfig
import lunatech.lunchplanner.data.ControllersData._
import lunatech.lunchplanner.models.{MenuDish, MenuPerDay, MenuPerDayPerPerson}
import lunatech.lunchplanner.persistence._
import org.scalatest.BeforeAndAfterEach

import java.sql.Date
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class ReportServiceSpec extends BehaviorTestingConfig with BeforeAndAfterEach {
  private val dishService       = new DishService()
  private val menuService       = new MenuService()
  private val menuPerDayService = new MenuPerDayService(menuService)
  private val menuPerDayPerPersonService =
    new MenuPerDayPerPersonService(dishService, menuService, menuPerDayService)
  private val reportService =
    new ReportService(menuPerDayPerPersonService, menuPerDayService)

  // scalastyle:off method.length
  override def beforeEach(): Unit = {
    createTestSchema()

    Await.result(
      for {
        _ <- UserTable.add(user1)
        _ <- UserTable.add(user2)
        _ <- UserTable.add(user3)
        _ <- UserTable.add(user4)

        _ <- UserProfileTable.add(userProfile1.copy(userUuid = user1.uuid))
        _ <- UserProfileTable.add(userProfile2.copy(userUuid = user2.uuid))
        _ <- UserProfileTable.add(userProfile3.copy(userUuid = user3.uuid))
        _ <- UserProfileTable.add(userProfile4.copy(userUuid = user4.uuid))

        _ <- DishTable.add(dish1)
        _ <- DishTable.add(dish2)
        _ <- DishTable.add(dish3)
        _ <- DishTable.add(dish4)
        _ <- DishTable.add(dish5)

        _ <- MenuTable.add(menu1)
        _ <- MenuTable.add(menu2)
        _ <- MenuDishTable.add(
          MenuDish(menuUuid = menu1.uuid, dishUuid = dish1.uuid)
        )
        _ <- MenuDishTable.add(
          MenuDish(menuUuid = menu1.uuid, dishUuid = dish2.uuid)
        )
        _ <- MenuDishTable.add(
          MenuDish(menuUuid = menu2.uuid, dishUuid = dish3.uuid)
        )
        _ <- MenuDishTable.add(
          MenuDish(menuUuid = menu2.uuid, dishUuid = dish4.uuid)
        )
        _ <- MenuDishTable.add(
          MenuDish(menuUuid = menu2.uuid, dishUuid = dish5.uuid)
        )

        menuperday1 <- MenuPerDayTable.add(
          MenuPerDay(
            menuUuid = menu1.uuid,
            date = Date.valueOf("2018-01-05"),
            location = "Rotterdam"
          )
        )
        menuperday2 <- MenuPerDayTable.add(
          MenuPerDay(
            menuUuid = menu2.uuid,
            date = Date.valueOf("2018-01-05"),
            location = "Amsterdam"
          )
        )
        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday1.uuid,
            userUuid = user1.uuid,
            isAttending = true
          )
        )
        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday2.uuid,
            userUuid = user2.uuid,
            isAttending = true
          )
        )
        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday1.uuid,
            userUuid = user3.uuid,
            isAttending = true
          )
        )
        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday1.uuid,
            userUuid = user4.uuid,
            isAttending = false
          )
        )
        menuperday3 <- MenuPerDayTable.add(
          MenuPerDay(
            menuUuid = menu1.uuid,
            date = Date.valueOf("2018-02-10"),
            location = "Rotterdam"
          )
        )
        menuperday4 <- MenuPerDayTable.add(
          MenuPerDay(
            menuUuid = menu2.uuid,
            date = Date.valueOf("2018-02-10"),
            location = "Amsterdam"
          )
        )

        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday4.uuid,
            userUuid = user1.uuid,
            isAttending = true
          )
        )
        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday4.uuid,
            userUuid = user2.uuid,
            isAttending = true
          )
        )
        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday4.uuid,
            userUuid = user3.uuid,
            isAttending = false
          )
        )
        _ <- MenuPerDayPerPersonTable.addOrUpdate(
          MenuPerDayPerPerson(
            menuPerDayUuid = menuperday4.uuid,
            userUuid = user4.uuid,
            isAttending = true
          )
        )
      } yield (),
      defaultTimeout
    )
  }

  override def afterEach(): Unit = dropTestSchema()

  "Report service" should {
    "produce list of attendees per location by date for January 2018" in {
      val result = Await.result(
        reportService.getReportByLocationAndDate(1, 2018),
        defaultTimeout
      )
      result.usersPerDateAndLocation mustBe List(
        ((Date.valueOf("2018-01-05"), 1, "Amsterdam"), Vector("user 2")),
        (
          (Date.valueOf("2018-01-05"), 1, "Rotterdam"),
          Vector("user 1", "user 3")
        )
      )
    }

    "produce list of attendees per location by date for February 2018" in {
      val result = Await.result(
        reportService.getReportByLocationAndDate(2, 2018),
        defaultTimeout
      )
      result.usersPerDateAndLocation mustBe List(
        (
          (Date.valueOf("2018-02-10"), 6, "Amsterdam"),
          Vector("user 1", "user 2", "user 4")
        )
      )
    }

    "produce list of non attendees by date in January" in {
      val result = Await.result(
        reportService.getReportNotAttendingByDate(1, 2018),
        defaultTimeout
      )
      result.usersPerDate mustBe List(
        (Date.valueOf("2018-01-05"), 1, Vector("user 4"))
      )
    }

    "produce list of non attendees by date in February" in {
      val result = Await.result(
        reportService.getReportNotAttendingByDate(2, 2018),
        defaultTimeout
      )
      result.usersPerDate mustBe List(
        (Date.valueOf("2018-02-10"), 6, Vector("user 3"))
      )
    }
  }

}
