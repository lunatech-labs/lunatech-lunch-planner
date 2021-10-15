package lunatech.lunchplanner.services

import java.util.UUID
import lunatech.lunchplanner.common.BehaviorTestingConfig
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration

import scala.concurrent.Await

class UserProfileServiceSpec extends BehaviorTestingConfig with BeforeAndAfterEach with MockFactory {
  private val userProfileService = new UserProfileService()

  override def beforeEach(): Unit = createTestSchema()

  override def afterEach(): Unit = dropTestSchema()

  "user profile service" should {
    "return none when getting user profile by non-existent ulunatech.lunchplanner.persistence.UserProfileTableSpecser uuid" in {
      val result = Await.result(userProfileService.getUserProfileByUserUuid(UUID.randomUUID), defaultTimeout)

      result mustBe None
    }
  }
}
