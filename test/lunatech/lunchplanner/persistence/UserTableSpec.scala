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

  private val newUserIsNotAdmin = User(UUID.randomUUID(), "Pedro Ferreira", "pedro.ferreira@lunatech.com")
  private val newUserIsAdmin = User(UUID.randomUUID(), "Leonor Boga", "leonor.boga@lunatech.com", isAdmin = true)

  "A User table" must {
    "add a new user that is not an admin user" in {
      val result = Await.result(UserTable.addUser(newUserIsNotAdmin), defaultTimeout)
      result mustBe newUserIsNotAdmin
    }

    "add a new user that is an admin user" in {
      val result = Await.result(UserTable.addUser(newUserIsAdmin), defaultTimeout)
      result mustBe newUserIsAdmin
    }

    "query for existing users successfully" in {
      val result = Await.result(UserTable.userExists(newUserIsAdmin.uuid), defaultTimeout)
      result mustBe true
    }

    "query for users by uuid" in {
      val result = Await.result(UserTable.getUserByUUID(newUserIsAdmin.uuid), defaultTimeout)
      result mustBe Some(newUserIsAdmin)
    }

    "query for users by email address" in {
      val result = Await.result(UserTable.getUserByEmailAddress(newUserIsAdmin.emailAddress), defaultTimeout)
      result mustBe Some(newUserIsAdmin)
    }

    "query all users" in {
      val result = Await.result(UserTable.getAllUsers, defaultTimeout)
      result mustBe Vector(newUserIsNotAdmin, newUserIsAdmin)
    }

    "remove an existing user by uuid" in {
      val result = Await.result(UserTable.removeUser(newUserIsAdmin.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a user that does not exist" in {
      val result = Await.result(UserTable.removeUser(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }
  }
}
