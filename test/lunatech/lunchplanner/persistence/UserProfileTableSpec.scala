package lunatech.lunchplanner.persistence

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.{ Menu, MenuPerDay, MenuPerDayPerPerson, User, UserProfile }

import scala.concurrent.Await

class UserProfileTableSpec extends AcceptanceSpec with TestDatabaseProvider {

  implicit private val dbConnection = app.injector.instanceOf[DBConnection]

  private val newUser = User(UUID.randomUUID(), "Leonor Boga", "leonor.boga@lunatech.com")
  private val newUserProfile = UserProfile(userUuid = newUser.uuid, vegetarian = true)

  override def beforeAll {
    cleanDatabase()
    Await.result(UserTable.add(newUser), defaultTimeout)
  }

  "A UserProfile table" must {
    "add a new user profile" in {
      val result = Await.result(UserProfileTable.insertOrUpdate(newUserProfile), defaultTimeout)
      result mustBe true
    }

    "get user profile by user uuid" in {
      val result = Await.result(UserProfileTable.getByUserUUID(newUser.uuid), defaultTimeout)
      result mustBe Some(newUserProfile)
    }

    "get all user profiles" in {
      val result = Await.result(UserProfileTable.getAll, defaultTimeout)
      result mustBe Seq(newUserProfile)
    }

    "remove user profile" in {
      val result = Await.result(UserProfileTable.remove(newUser.uuid), defaultTimeout)
      result mustBe 1
    }

    "give summary of diet restrictions by menuPerDay" in {
      val newMenu = Menu(name = "Main menu")
      val newMenuPerDay = MenuPerDay(menuUuid = newMenu.uuid, date = new Date(99999999))
      val newMenuPerDayPerPerson = MenuPerDayPerPerson(menuPerDayUuid = newMenuPerDay.uuid, userUuid = newUser.uuid)

      Await.result(UserProfileTable.insertOrUpdate(newUserProfile), defaultTimeout)
      Await.result(MenuTable.add(newMenu), defaultTimeout)
      Await.result(MenuPerDayTable.add(newMenuPerDay), defaultTimeout)
      Await.result(MenuPerDayPerPersonTable.add(newMenuPerDayPerPerson), defaultTimeout)

      val result = Await.result(UserProfileTable.getRestrictionsByMenuPerDay(newMenuPerDay.uuid), defaultTimeout)
      result mustBe Vector((1, 0, 0, 0, 0, 0, 0))
    }
  }
}
