package lunatech.lunchplanner.controllers

import java.util.UUID

import scala.concurrent.Future

import akka.stream.Materializer
import com.lunatech.openconnect.{APISessionCookieBaker, Authenticate}
import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.common.ControllerSpec
import lunatech.lunchplanner.models.{Event, User}
import lunatech.lunchplanner.services.{RestService, UserService}
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json
import play.api.{Configuration, Environment}
import play.api.http.{SecretConfiguration, SessionConfiguration}
import play.api.mvc.{
  AnyContentAsEmpty,
  ControllerComponents,
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
import play.api.libs.ws.WSClient
import lunatech.lunchplanner.services.MenuPerDayPerPersonService

// scalastyle:off magic.number
class RestControllerSpec extends ControllerSpec with MockFactory {

  implicit private lazy val materializer: Materializer = app.materializer

  private val developer =
    User(UUID.randomUUID, "Developer", "developer@lunatech.nl", isAdmin = true)
  private val bearerToken       = "testBearerToken"
  private val bearerTokenHeader = "Bearer testBearerToken"

  private val userService                = mock[UserService]
  private val restService                = mock[RestService]
  private val menuPerDayPerPersonService = mock[MenuPerDayPerPersonService]

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

  class MockedAuthenticate
      extends Authenticate(configuration, mock[WSClient]) {}

  private val controllerComponents =
    app.injector.instanceOf[ControllerComponents]

  private val controller = new RestController(
    userService = userService,
    restService = restService,
    menuPerDayPerPersonService = menuPerDayPerPersonService,
    environment = mock[Environment],
    authenticate = mock[MockedAuthenticate],
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

    "return jwt token" in new WithApplication() {
      (cookieBaker.jwtCodec.encode _)
        .expects(
          Map[String, String](
            "email"   -> developer.emailAddress,
            "isAdmin" -> developer.isAdmin.toString
          )
        )
        .returns("testToken")

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest()
          .withSession("email" -> developer.emailAddress)
          .withHeaders("Authorization" -> bearerTokenHeader)
      val result: Future[Result] =
        call(controller.validateAccessToken("dummyAccessToken"), request)

      status(result) mustBe 200
      contentAsString(result) mustBe "testToken"
    }
  }
}
