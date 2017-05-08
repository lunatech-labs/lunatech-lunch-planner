package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.User

import scala.concurrent.Await
import scala.concurrent.duration._

class UserTableSpec extends AcceptanceSpec with TestDatabaseProvider {

  implicit private val dbConnection = app.injector.instanceOf[DBConnection]

  override def beforeAll {
    cleanDatabase()
  }

  private val newUser = User(UUID.randomUUID(), "Leonor Boga", "leonor.boga@lunatech.com")
  private val newUser2 = User(UUID.randomUUID(), "Pedro Ferreira", "pedro.ferreira@lunatech.com")

  "A User table" must {
    "add a new user" in {
      val result = Await.result(UserTable.add(newUser), defaultTimeout)
      result mustBe newUser
    }

    "query for existing users successfully" in {
      val result = Await.result(UserTable.exists(newUser.uuid), defaultTimeout)
      result mustBe true
    }

    "query for users by uuid" in {
      val result = Await.result(UserTable.getByUUID(newUser.uuid), defaultTimeout)
      result mustBe Some(newUser)
    }

    "query for users by email address" in {
      val result = Await.result(UserTable.getByEmailAddress(newUser.emailAddress), defaultTimeout)
      result mustBe Some(newUser)
    }

    "query all users" in {
      Await.result(UserTable.add(newUser2), defaultTimeout)
      val result = Await.result(UserTable.getAll, defaultTimeout)
      result mustBe Vector(newUser, newUser2)
    }

    "remove an existing user by uuid" in {
      val result = Await.result(UserTable.remove(newUser.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a user that does not exist" in {
      val result = Await.result(UserTable.remove(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }
  }
}
