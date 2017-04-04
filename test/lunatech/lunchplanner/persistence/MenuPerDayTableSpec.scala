package lunatech.lunchplanner.persistence

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.{ Menu, MenuPerDay }

import scala.concurrent.Await
import scala.concurrent.duration._

class MenuPerDayTableSpec extends AcceptanceSpec with TestDatabaseProvider {
  implicit private val dbConnection = app.injector.instanceOf[DBConnection]
  private val defaultTimeout = 10.seconds

  private val newMenu = Menu(name = "Main menu")
  private val newMenuPerDay = MenuPerDay(menuUuid = newMenu.uuid, date = new Date(10000))

  override def beforeAll {
    cleanDatabase()

    Await.result(MenuTable.addMenu(newMenu), defaultTimeout)
  }

  "A MenuPerDay table" must {
    "add a new menu per day" in {
      val result = Await.result(MenuPerDayTable.addMenuPerDay(newMenuPerDay), defaultTimeout)
      result.uuid mustBe newMenuPerDay.uuid
      result.menuUuid mustBe newMenuPerDay.menuUuid
      result.date.toString mustBe newMenuPerDay.date.toString
    }

    "query for existing menus per day successfully" in {
      val result = Await.result(MenuPerDayTable.menuPerDayExists(newMenuPerDay.uuid), defaultTimeout)
      result mustBe true
    }

    "query for menus per day by uuid" in {
      val result = Await.result(MenuPerDayTable.getMenuPerDayByUUID(newMenuPerDay.uuid), defaultTimeout)
      result.foreach(_.date.toString) mustBe Some(newMenuPerDay).foreach(_.date.toString)
    }

    "query for menus per day by menu uuid" in {
      val result = Await.result(MenuPerDayTable.getMenuPerDayByMenuUuid(newMenu.uuid), defaultTimeout)
      result.foreach(_.date.toString) mustBe Vector(newMenuPerDay).foreach(_.date.toString)
    }

    "query for menus per day by non existent menu uuid" in {
      val result = Await.result(MenuPerDayTable.getMenuPerDayByMenuUuid(UUID.randomUUID()), defaultTimeout)
      result mustBe Vector()
    }

    "query for menus per day by date" in {
      val result = Await.result(MenuPerDayTable.getMenuPerDayByDate(new Date(10000)), defaultTimeout)
      result.foreach(_.date.toString) mustBe Vector(newMenuPerDay).foreach(_.date.toString)
    }

    "query for menus per day by date that does not exist in table" in {
      val result = Await.result(MenuPerDayTable.getMenuPerDayByDate(new Date(900000000)), defaultTimeout)
      result mustBe Vector()
    }

    "query all menus per day" in {
      val result = Await.result(MenuPerDayTable.getAllMenuPerDays, defaultTimeout)
      result.foreach(_.date.toString) mustBe Vector(newMenuPerDay).foreach(_.date.toString)
    }

    "remove an existing menu per day by uuid" in {
      val result = Await.result(MenuPerDayTable.removeMenuPerDay(newMenuPerDay.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a menu per day that does not exist" in {
      val result = Await.result(MenuPerDayTable.removeMenuPerDay(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }
  }
}
