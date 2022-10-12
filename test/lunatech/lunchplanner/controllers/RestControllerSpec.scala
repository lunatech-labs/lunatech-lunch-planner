package lunatech.lunchplanner.controllers

import java.util.UUID

import scala.concurrent.Future

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.common.ControllerSpec
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.services.UserService
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json
import play.api.Configuration
import play.api.mvc.{
  ControllerComponents,
  DefaultSessionCookieBaker,
  JWTCookieDataCodec
}
import play.api.test.FakeRequest
import play.api.test.Helpers.{
  call,
  contentAsString,
  defaultAwaitTimeout,
  status
}
import play.test.WithApplication

class RestControllerSpec extends ControllerSpec with MockFactory {

  implicit private lazy val materializer: Materializer = app.materializer

  private val developer =
    User(UUID.randomUUID, "Developer", "developer@lunatech.nl")
  private val bearerToken       = "testBearerToken"
  private val bearerTokenHeader = "Bearer testBearerToken"

  private val userService = mock[UserService]

  private val cookieDataCodec = mock[JWTCookieDataCodec]
  class MockedCookieBaker extends DefaultSessionCookieBaker {
    override val jwtCodec: JWTCookieDataCodec = cookieDataCodec
  }
  private val cookieBaker: MockedCookieBaker = mock[MockedCookieBaker]

  private val controllerComponents =
    app.injector.instanceOf[ControllerComponents]
  private val configuration = Configuration(
    ConfigFactory.load("application-test.conf")
  )

  private val controller = new RestController(
    userService = userService,
    sessionCookieBaker = cookieBaker,
    configuration = configuration,
    controllerComponents = controllerComponents
  )

  "Rest controller" should {
    "return json for a user if it exists" in new WithApplication() {
      (userService.getByEmailAddress _)
        .expects(developer.emailAddress)
        .returns(Future.successful(Some(developer)))
      (cookieBaker.jwtCodec.decode _)
        .expects(bearerToken)
        .returns(Map[String, String]("email" -> developer.emailAddress))

      val request =
        FakeRequest()
          .withSession("email" -> developer.emailAddress)
          .withHeaders("Authorization" -> bearerTokenHeader)
      val result = call(controller.getUser(developer.emailAddress), request)

      status(result) mustBe 200
      contentAsString(result) mustBe Json.toJson(developer).toString()
    }

    "return 404 if user does not exist" in new WithApplication() {
      private val notRegisteredEmail = "not@registered.com"
      (userService.getByEmailAddress _)
        .expects(notRegisteredEmail)
        .returns(Future.successful(None))
      (cookieBaker.jwtCodec.decode _)
        .expects(bearerToken)
        .returns(Map[String, String]("email" -> developer.emailAddress))

      val request =
        FakeRequest()
          .withSession("email" -> developer.emailAddress)
          .withHeaders("Authorization" -> bearerTokenHeader)
      val result = call(controller.getUser(notRegisteredEmail), request)

      status(result) mustBe 404
      contentAsString(result) mustBe "User not found"
    }
  }

}
