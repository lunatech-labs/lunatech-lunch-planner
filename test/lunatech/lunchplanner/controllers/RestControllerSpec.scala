package lunatech.lunchplanner.controllers

import java.util.UUID
import java.sql.Date
import java.time.Instant

import scala.concurrent.Future

import akka.stream.Materializer
import com.lunatech.openconnect.APISessionCookieBaker
import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.common.ControllerSpec
import lunatech.lunchplanner.models.{Dish, Event, User}
import lunatech.lunchplanner.services.{
  DishService,
  MenuDishService,
  MenuService,
  RestService,
  UserService
}
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json
import play.api.Configuration
import play.api.http.{SecretConfiguration, SessionConfiguration}
import play.api.mvc.{
  AnyContentAsEmpty,
  ControllerComponents,
  DefaultSessionCookieBaker,
  JWTCookieDataCodec,
  Result
}
import play.api.test.FakeRequest
import play.api.test.Helpers.{
  call,
  contentAsString,
  defaultAwaitTimeout,
  status
}
import play.test.WithApplication
import scala.concurrent.ExecutionContext.Implicits.global

import lunatech.lunchplanner.data.ControllersData

// scalastyle:off magic.number
class RestControllerSpec extends ControllerSpec with MockFactory {

  implicit private lazy val materializer: Materializer = app.materializer

  private val developer =
    User(UUID.randomUUID, "Developer", "developer@lunatech.nl")
  private val bearerToken       = "testBearerToken"
  private val bearerTokenHeader = "Bearer testBearerToken"

  private val userService = mock[UserService]
  private val restService = mock[RestService]

  private val configuration = Configuration(
    ConfigFactory.load("application-test.conf")
  )

  private val cookieDataCodec = mock[JWTCookieDataCodec]
  class MockedCookieBaker
      extends APISessionCookieBaker(
        configuration: Configuration,
        new SecretConfiguration: SecretConfiguration,
        new SessionConfiguration: SessionConfiguration
      ) {
    override val jwtCodec: JWTCookieDataCodec = cookieDataCodec
  }
  private val cookieBaker: MockedCookieBaker = mock[MockedCookieBaker]

  private val controllerComponents =
    app.injector.instanceOf[ControllerComponents]

  private val controller = new RestController(
    userService = userService,
    restService = restService,
    apiSessionCookieBaker = cookieBaker,
    configuration = configuration,
    controllerComponents = controllerComponents
  )

  private val events: Seq[Event] = ControllersData.events

  "Rest controller" should {
    "return json for a user if it exists" in new WithApplication() {
      (userService.getByEmailAddress _)
        .expects(developer.emailAddress)
        .returns(Future.successful(Some(developer)))
      (cookieBaker.jwtCodec.decode _)
        .expects(bearerToken)
        .returns(Map[String, String]("email" -> developer.emailAddress))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest()
          .withSession("email" -> developer.emailAddress)
          .withHeaders("Authorization" -> bearerTokenHeader)
      val result: Future[Result] =
        call(controller.getUser(developer.emailAddress), request)

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

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest()
          .withSession("email" -> developer.emailAddress)
          .withHeaders("Authorization" -> bearerTokenHeader)
      val result: Future[Result] =
        call(controller.getUser(notRegisteredEmail), request)

      status(result) mustBe 404
      contentAsString(result) mustBe "User not found"
    }

    "return json for future events" in new WithApplication() {
      (restService.getFutureEvents _)
        .expects(developer, 10)
        .returns(Future.successful(events))
      (userService.getByEmailAddress _)
        .expects(developer.emailAddress)
        .returns(Future.successful(Some(developer)))
      (cookieBaker.jwtCodec.decode _)
        .expects(bearerToken)
        .returns(Map[String, String]("email" -> developer.emailAddress))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest()
          .withSession("email" -> developer.emailAddress)
          .withHeaders("Authorization" -> bearerTokenHeader)
      val result: Future[Result] =
        call(controller.getFutureEvents(10), request)

      status(result) mustBe 200
      contentAsString(result) mustBe Json.toJson(events).toString()
    }
  }

}
