package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.User
import org.scalacheck._
import org.scalacheck.Prop._

import scala.concurrent.Await
import shapeless.contrib.scalacheck._

object UserTableSpec extends Properties("UserProfile") with PropertyTestingConfig {

  import TableDataGenerator._

  override def afterAll(): Unit = dbConnection.db.close()

  property("add a new user") = forAll { user: User =>
    val result = addUserToDB(user)

    cleanUserTableProps

    result == user
  }

  property("query for existing users successfully") = forAll { user: User =>
    addUserToDB(user)

    val result = Await.result(UserTable.exists(user.uuid), defaultTimeout)

    cleanUserTableProps

    result
  }

  property("query for existing users successfully") = forAll { user: User =>
    addUserToDB(user)

    val result = Await.result(UserTable.getByUUID(user.uuid), defaultTimeout).get

    cleanUserTableProps

    result == user
  }

  property("query for users by email address") = forAll { user: User =>
    addUserToDB(user)

    val result = Await.result(UserTable.getByEmailAddress(user.emailAddress), defaultTimeout).get

    cleanUserTableProps

    result == user
  }

  property("query for users by email address") = forAll { (user1: User, user2: User) =>
    addUserToDB(user1)
    addUserToDB(user2)

    val result = Await.result(UserTable.getAll, defaultTimeout)

    cleanUserTableProps

    result == Seq(user1, user2)
  }

  property("remove an existing user by uuid") = forAll { user: User =>
    addUserToDB(user)

    val result = Await.result(UserTable.remove(user.uuid), defaultTimeout)

    cleanUserTableProps

    result == 1
  }

  property("remove an existing user by uuid") = forAll { user: User =>
    //skip adding user to DB

    val result = Await.result(UserTable.remove(user.uuid), defaultTimeout)
    result == 0
  }

  private def addUserToDB(user: User) = {
    Await.result(UserTable.add(user), defaultTimeout)
  }

  private def cleanUserTableProps = {
    cleanDatabase
    true
  }
}
