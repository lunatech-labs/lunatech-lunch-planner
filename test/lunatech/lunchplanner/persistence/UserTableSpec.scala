package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.User

import scala.concurrent.Await
import scala.concurrent.duration._

class UserTableSpec extends AcceptanceSpec with TestDatabaseProvider {

  implicit private val dbConnection = app.injector.instanceOf[DBConnection]
  private val defaultTimeout = 10.seconds

  override def beforeAll {
    cleanDatabase()
  }

  private val newUser = User(UUID.randomUUID(), "Leonor Boga", "leonor.boga@lunatech.com")
  private val newUser2 = User(UUID.randomUUID(), "Pedro Ferreira", "pedro.ferreira@lunatech.com")

  "A User table" must {
    "add a new user" in {
      val result = Await.result(UserTable.addUser(newUser), defaultTimeout)
      result mustBe newUser
    }

    "query for existing users successfully" in {
      val result = Await.result(UserTable.userExists(newUser.uuid), defaultTimeout)
      result mustBe true
    }

    "query for users by uuid" in {
      val result = Await.result(UserTable.getUserByUUID(newUser.uuid), defaultTimeout)
      result mustBe Some(newUser)
    }

    "query for users by email address" in {
      val result = Await.result(UserTable.getUserByEmailAddress(newUser.emailAddress), defaultTimeout)
      result mustBe Some(newUser)
    }

    "query all users" in {
      Await.result(UserTable.addUser(newUser2), defaultTimeout)
      val result = Await.result(UserTable.getAllUsers, defaultTimeout)
      result mustBe Vector(newUser, newUser2)
    }

    "remove an existing user by uuid" in {
      val result = Await.result(UserTable.removeUser(newUser.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a user that does not exist" in {
      val result = Await.result(UserTable.removeUser(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }
  }
}
