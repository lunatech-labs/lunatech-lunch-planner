package lunatech.lunchplanner.services

import java.util.UUID

import lunatech.lunchplanner.common.BehaviorTestingConfig
import play.api.Configuration

import scala.concurrent.Await

class UserProfileServiceSpec extends BehaviorTestingConfig {

  private val configuration = mock[Configuration]
  private val userProfileService = new UserProfileService(configuration)

  override def afterAll(): Unit = cleanUserAndProfileTable

  "user profile service" should {
    "return none when getting user profile by non-existent user uuid" in {
      val result = Await.result(userProfileService.getUserProfileByUserUuid(UUID.randomUUID), defaultTimeout)

      result mustBe None
    }
  }
}
