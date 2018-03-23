package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.BehaviorTestingConfig
import lunatech.lunchplanner.persistence.UserTable
import play.api.Configuration

import org.mockito.Mockito._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class UserServiceSpec extends BehaviorTestingConfig {

  import lunatech.lunchplanner.data.ControllersData._

  private val configuration = mock[Configuration]
  private val userService = new UserService(configuration)

  override def beforeAll: Unit = {
    when(configuration.get[Seq[String]]("administrators")).thenReturn(Seq("developer@lunatech.com", "user1@lunatech.com"))

    Await.result(
      for {
        _ <- UserTable.add(user1)
        _ <- UserTable.add(user2)
      } yield (), defaultTimeout)
  }

  override def afterAll(): Unit = cleanUserAndProfileTable

  "user service" should {
    "return user by email address" in {
      val result = Await.result(userService.getByEmailAddress(user1.emailAddress), defaultTimeout).get

      result mustBe user1
    }

    "return nothing when searching user by email address that does not exist" in {
      val result = Await.result(userService.getByEmailAddress("no-user@mail.com"), defaultTimeout)

      result mustBe None
    }

    "return if user has admin privileges (when true)" in {
      val result = userService.isAdminUser(user1.emailAddress)

      result mustBe true
    }

    "return if user has admin privileges (when false)" in {
      val result = userService.isAdminUser(user2.emailAddress)

      result mustBe false
    }

    "return all users' email addresses when there are users" in {
      val result = Await.result(userService.getAllEmailAddresses, defaultTimeout)

      result must contain (user1.emailAddress)
      result must contain (user2.emailAddress)
      result mustNot contain (user3.emailAddress)
    }

    "not add new users when user already exists" in {
      val result = Await.result(userService.addUserIfNew(user1.emailAddress), defaultTimeout)

      result mustBe false
    }

    "add new users when user does not exists" in {
      val result = Await.result(userService.addUserIfNew(user4.emailAddress), defaultTimeout)

      result mustBe true
    }
  }
}
