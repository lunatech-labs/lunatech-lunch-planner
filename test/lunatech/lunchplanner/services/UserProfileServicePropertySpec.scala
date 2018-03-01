package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.{ User, UserProfile }
import lunatech.lunchplanner.persistence.{ UserProfileTable, UserTable }
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration

import scala.concurrent.Await

object UserProfileServicePropertySpec extends Properties("UserService") with PropertyTestingConfig with MockitoSugar {

  import lunatech.lunchplanner.data.TableDataGenerator._

  override def afterAll(): Unit = dbConnection.db.close()

  private val configuration = mock[Configuration]
  private val userProfileService = new UserProfileService(configuration)

  property("insert new user profile") = forAll { (user: User, userProfile: UserProfile) =>
    addUserToDB(user)
    val result = Await.result(userProfileService.insertOrUpdate(userProfile.copy(userUuid = user.uuid)), defaultTimeout)

    val newProfile = Await.result(UserProfileTable.getByUserUUID(user.uuid), defaultTimeout).get

    cleanUserAndProfileTable

    result && newProfile == userProfile.copy(userUuid = user.uuid)
  }

  property("update an existent user profile") = forAll { (user: User, userProfile1: UserProfile, userProfile2: UserProfile) =>
    addUserAndProfileToDB(user, userProfile1)

    val result = Await.result(userProfileService.insertOrUpdate(userProfile2.copy(userUuid = user.uuid)), defaultTimeout)
    val updatedProfile = Await.result(UserProfileTable.getByUserUUID(user.uuid), defaultTimeout).get

    cleanUserAndProfileTable

    result && updatedProfile == userProfile2.copy(userUuid = user.uuid)
  }

  property("return user profile given user uuid") = forAll { (user: User, userProfile: UserProfile) =>
    addUserAndProfileToDB(user, userProfile)

    val result = Await.result(userProfileService.getUserProfileByUserUuid(user.uuid), defaultTimeout).get

    cleanUserAndProfileTable

    result == userProfile.copy(userUuid = user.uuid)
  }

  private def addUserToDB(user: User) = {
    Await.result(UserTable.add(user), defaultTimeout)
  }

  private def addUserAndProfileToDB(user: User, userProfile: UserProfile) = {
    Await.result(UserTable.add(user), defaultTimeout)
    Await.result(UserProfileTable.insertOrUpdate(userProfile.copy(userUuid = user.uuid)), defaultTimeout)
  }
}
