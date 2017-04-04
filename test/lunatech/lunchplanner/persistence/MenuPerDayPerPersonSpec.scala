package lunatech.lunchplanner.persistence

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.{ Menu, MenuPerDay, MenuPerDayPerPerson, User }

import scala.concurrent.Await
import scala.concurrent.duration._

class MenuPerDayPerPersonTableSpec extends AcceptanceSpec with TestDatabaseProvider {
  implicit private val dbConnection = app.injector.instanceOf[DBConnection]
  private val defaultTimeout = 10.seconds

  private val newUser = User(name = "Leonor Boga", emailAddress ="leonor.boga@lunatech.com", isAdmin = true)
  private val newMenu = Menu(name = "Main menu")
  private val newMenuPerDay = MenuPerDay(menuUuid = newMenu.uuid, date = new Date(3000000))

  private val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = newMenuPerDay.uuid, userUuid = newUser.uuid)

  override def beforeAll {
    cleanDatabase()

    Await.result(UserTable.addUser(newUser), defaultTimeout)
    Await.result(MenuTable.addMenu(newMenu), defaultTimeout)
    Await.result(MenuPerDayTable.addMenuPerDay(newMenuPerDay), defaultTimeout)
  }

  "A MenuPerDayPerPerson table" must {
    "add a new menu per day per person" in {
      val result = Await.result(MenuPerDayPerPersonTable.addMenuPerDayPerPerson(newMenuPerDayPerPerson), defaultTimeout)
      result mustBe newMenuPerDayPerPerson
    }

    "query for existing menus per day per person successfully" in {
      val result = Await.result(MenuPerDayPerPersonTable.menuPerDayPerPersonExists(newMenuPerDayPerPerson.uuid), defaultTimeout)
      result mustBe true
    }

    "query for menus per day per person by uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getMenuPerDayPerPersonByUUID(newMenuPerDayPerPerson.uuid), defaultTimeout)
      result mustBe Some(newMenuPerDayPerPerson)
    }

    "query for menus per day per person by menu per person uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getMenuPerDayPerPersonByMenuPerDayUuid(newMenuPerDay.uuid), defaultTimeout)
      result mustBe Vector(newMenuPerDayPerPerson)
    }

    "query for menus per day per person by non existent menu per person uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getMenuPerDayPerPersonByMenuPerDayUuid(UUID.randomUUID()), defaultTimeout)
      result mustBe Vector()
    }

    "query for menus per day per person by user uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getMenuPerDayPerPersonByUserUuid(newUser.uuid), defaultTimeout)
      result mustBe Vector(newMenuPerDayPerPerson)
    }

    "query for menus per day per person by user uuid that does not exist in table" in {
      val result = Await.result(MenuPerDayPerPersonTable.getMenuPerDayPerPersonByUserUuid(UUID.randomUUID()), defaultTimeout)
      result mustBe Vector()
    }

    "query all menus per day per person" in {
      val result = Await.result(MenuPerDayPerPersonTable.getAllMenuPerDayPerPersons, defaultTimeout)
      result mustBe Vector(newMenuPerDayPerPerson)
    }

    "remove an existing menu per day per person by uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.removeMenuPerDayPerPerson(newMenuPerDayPerPerson.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a menu per day per person that does not exist" in {
      val result = Await.result(MenuPerDayPerPersonTable.removeMenuPerDayPerPerson(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }
  }
}
