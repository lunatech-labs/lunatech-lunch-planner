package lunatech.lunchplanner.persistence

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.{ Menu, MenuPerDay, MenuPerDayPerPerson, User, UserProfile }

import scala.concurrent.Await
import scala.concurrent.duration._

class MenuPerDayPerPersonTableSpec extends AcceptanceSpec with TestDatabaseProvider {
  implicit private val dbConnection = app.injector.instanceOf[DBConnection]

  private val newUser = User(name = "Leonor Boga", emailAddress ="leonor.boga@lunatech.com")
  private val newUserProfile = UserProfile(newUser.uuid)
  private val newMenu = Menu(name = "Main menu")
  private val newMenuPerDay = MenuPerDay(menuUuid = newMenu.uuid, date = new Date(3000000), location = "Amsterdam")

  private val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = newMenuPerDay.uuid, userUuid = newUser.uuid, isAttending = false)

  override def beforeAll {
    cleanDatabase()

    Await.result(UserTable.add(newUser), defaultTimeout)
    Await.result(UserProfileTable.insertOrUpdate(newUserProfile), defaultTimeout)
    Await.result(MenuTable.add(newMenu), defaultTimeout)
    Await.result(MenuPerDayTable.add(newMenuPerDay), defaultTimeout)
  }

  "A MenuPerDayPerPerson table" must {
    "add a new menu per day per person" in {
      val result = Await.result(MenuPerDayPerPersonTable.add(newMenuPerDayPerPerson), defaultTimeout)
      result mustBe newMenuPerDayPerPerson
    }

    "query for existing menus per day per person successfully" in {
      val result = Await.result(MenuPerDayPerPersonTable.exists(newMenuPerDayPerPerson.uuid), defaultTimeout)
      result mustBe true
    }

    "query for menus per day per person by uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getByUUID(newMenuPerDayPerPerson.uuid), defaultTimeout)
      result mustBe Some(newMenuPerDayPerPerson)
    }

    "query for menus per day per person by menu per person uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getByMenuPerDayUuid(newMenuPerDay.uuid), defaultTimeout)
      result mustBe Vector(newMenuPerDayPerPerson)
    }

    "query for menus per day per person by non existent menu per person uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getByMenuPerDayUuid(UUID.randomUUID()), defaultTimeout)
      result mustBe Vector()
    }

    "query for menus per day per person by user uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getByUserUuid(newUser.uuid), defaultTimeout)
      result mustBe Vector(newMenuPerDayPerPerson)
    }

    "query for menus per day per person by user uuid that does not exist in table" in {
      val result = Await.result(MenuPerDayPerPersonTable.getByUserUuid(UUID.randomUUID()), defaultTimeout)
      result mustBe Vector()
    }

    "query all menus per day per person" in {
      val result = Await.result(MenuPerDayPerPersonTable.getAll, defaultTimeout)
      result mustBe Vector(newMenuPerDayPerPerson)
    }

    "query for menu per day per person by user uuid and menu per person uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.getByUserUuidAndMenuPerDayUuid(newUser.uuid, newMenuPerDay.uuid), defaultTimeout)
      result mustBe Some(newMenuPerDayPerPerson)
    }

    "remove an existing menu per day per person by uuid" in {
      val result = Await.result(MenuPerDayPerPersonTable.remove(newMenuPerDayPerPerson.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a menu per day per person that does not exist" in {
      val result = Await.result(MenuPerDayPerPersonTable.remove(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }

    "remove an existing menu per day per person by menu per day uuid" in {
      Await.result(MenuPerDayPerPersonTable.add(newMenuPerDayPerPerson), defaultTimeout)
      val result = Await.result(MenuPerDayPerPersonTable.removeByMenuPerDayUuid(newMenuPerDayPerPerson.menuPerDayUuid), defaultTimeout)
      result mustBe 1
    }

    "query the list of people by menu per day" in {
      Await.result(MenuPerDayPerPersonTable.add(newMenuPerDayPerPerson), defaultTimeout)
      val result = Await.result(MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(newMenuPerDay.uuid, false), defaultTimeout)
      result mustBe Vector((newUser, newUserProfile))
    }
  }
}
