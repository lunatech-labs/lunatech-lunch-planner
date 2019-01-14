package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.User
import org.scalacheck._
import org.scalacheck.Prop._

import scala.concurrent.Await
import shapeless.contrib.scalacheck._

object UserTableSpec extends Properties(name = "User") with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  property("add a new user") = forAll { user: User =>
    createTestSchema()
    
    val result = addUserToDB(user)

    dropTestSchema()

    result == user
  }

  property("query for users by UUID successfully") = forAll { user: User =>
    createTestSchema()
    
    addUserToDB(user)

    val result = Await.result(UserTable.getByUUID(user.uuid), defaultTimeout).get

    dropTestSchema()

    result == user
  }

  property("query for users by email address") = forAll { user: User =>
    createTestSchema()
    
    addUserToDB(user)

    val result = Await.result(UserTable.getByEmailAddress(user.emailAddress), defaultTimeout).get

    dropTestSchema()

    result == user
  }

  property("check if a user exist by email address") = forAll { user: User =>
    createTestSchema()
    
    addUserToDB(user)

    val result = Await.result(UserTable.existsByEmail(user.emailAddress), defaultTimeout)

    dropTestSchema()

    result
  }

  property("query for all users") = forAll { (user1: User, user2: User) =>
    createTestSchema()
    
    addUserToDB(user1)
    addUserToDB(user2)

    val result = Await.result(UserTable.getAll, defaultTimeout)

    dropTestSchema()

    result == Seq(user1, user2)
  }

  property("delete an existing user by uuid") = forAll { user: User =>
    createTestSchema()
    
    addUserToDB(user)

    val deletedResult = Await.result(UserTable.removeByUuid(user.uuid), defaultTimeout)
    val getByUUIDUser = Await.result(UserTable.getByUUID(user.uuid), defaultTimeout).get

    dropTestSchema()

    deletedResult == 1 && getByUUIDUser.isDeleted
  }

  property("delete a non existing user by uuid") = forAll { user: User =>
    createTestSchema()
    
    //skip adding user to DB

    val deletedResult = Await.result(UserTable.removeByUuid(user.uuid), defaultTimeout)

    dropTestSchema()

    deletedResult == 0
  }

  private def addUserToDB(user: User): User = Await.result(UserTable.add(user), defaultTimeout)
}
