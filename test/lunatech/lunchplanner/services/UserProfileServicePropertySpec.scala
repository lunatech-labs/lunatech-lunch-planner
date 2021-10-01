package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.{ User, UserProfile }
import lunatech.lunchplanner.persistence.{ UserProfileTable, UserTable }
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalamock.scalatest.MockFactory
import play.api.Configuration

import scala.concurrent.Await

object UserProfileServicePropertySpec extends Properties("UserService") with PropertyTestingConfig with MockFactory {

  import lunatech.lunchplanner.data.TableDataGenerator._

  private val configuration = mock[Configuration]
  private val userProfileService = new UserProfileService(configuration)

  property("insert new user profile") = forAll { (user: User, userProfile: UserProfile) =>
    createTestSchema()
    
    addUserAndProfileToDB(user, userProfile)

    val result = Await.result(UserProfileTable.getByUserUUID(user.uuid), defaultTimeout).get

    dropTestSchema()

    result == userProfile.copy(userUuid = user.uuid)
  }

  property("update an existent user profile") = forAll { (user: User, userProfile1: UserProfile, userProfile2: UserProfile) =>
    createTestSchema()
    
    addUserAndProfileToDB(user, userProfile1)

    val result = Await.result(userProfileService.update(userProfile2.copy(userUuid = user.uuid)), defaultTimeout)
    val updatedProfile = Await.result(UserProfileTable.getByUserUUID(user.uuid), defaultTimeout).get

    dropTestSchema()

    result && updatedProfile == userProfile2.copy(userUuid = user.uuid)
  }

  property("return user profile given user uuid") = forAll { (user: User, userProfile: UserProfile) =>
    createTestSchema()
    
    addUserAndProfileToDB(user, userProfile)

    val result = Await.result(userProfileService.getUserProfileByUserUuid(user.uuid), defaultTimeout).get

    dropTestSchema()

    result == userProfile.copy(userUuid = user.uuid)
  }

  private def addUserAndProfileToDB(user: User, userProfile: UserProfile): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val query = for {
      _ <- UserTable.add(user)
      _ <- userProfileService.add(userProfile.copy(userUuid = user.uuid))
    } yield ()

    Await.result(query, defaultTimeout)
  }
}
