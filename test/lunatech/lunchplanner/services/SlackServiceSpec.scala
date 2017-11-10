package lunatech.lunchplanner.services

import java.util.UUID

import akka.stream.Materializer
import lunatech.lunchplanner.common.ControllerSpec
import lunatech.lunchplanner.models.User
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.libs.Json

class SlackServiceSpec extends ControllerSpec {

  implicit lazy val materializer: Materializer = app.materializer

  val user = User(UUID.randomUUID(), "Developer", "developer@lunatech.com", isAdmin = true)

  val userService = mock[UserService]
  val menuPerDayPerPersonService = mock[MenuPerDayPerPersonService]
  val menuPerDayService = mock[MenuPerDayService]
  val wsClient = mock[WSClient]
  val configuration = mock[Configuration]

  val slackService = new SlackService(userService,
    menuPerDayPerPersonService, menuPerDayService,
    wsClient, configuration)

  val json = Json.parse("")

  "The Slack controller" must {
    "register response for attending" in {
      
    }
  }
}
