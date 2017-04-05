package lunatech.lunchplanner.controllers

import java.net.URL

import com.google.gdata.client.appsforyourdomain.UserService
import com.google.gdata.client.authn.oauth.{ GoogleOAuthParameters, OAuthHmacSha1Signer }
import com.google.gdata.data.appsforyourdomain.provisioning.UserFeed
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.persistence.UserTable
import play.api.mvc.{ Action, RequestHeader, Result, Results, Security, _ }
import play.api.{ Configuration, Environment, Mode }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Provide security features
  */
trait Secured {

  val configuration: Configuration
  val environment: Environment
  implicit val connection: DBConnection

  def IsAdmin(f: => String => Request[AnyContent] => Future[Result]) = IsAuthenticated { userEmailAddress =>
    request =>
      val isAdminResult = UserTable.isAdminUser(userEmailAddress)(connection)
      isAdminResult.flatMap{ isUserAdmin =>
        if (isUserAdmin) {
          f(userEmailAddress)(request)
        } else {
          Future.successful(Results.Forbidden("you are not admin"))
        }
      }

  }

  /**
    * Action for authenticated users.
    */

  def IsAuthenticated(f: => String => Request[AnyContent] => Future[Result]) =
    Security.Authenticated(username, onUnauthorized) { user =>
      Action.async(request => f(user)(request))
    }

  /**
    * Retrieve the connected user email.
    */
  private def username(request: RequestHeader) =
    request.session.get("email")

  /**
    * Redirect to login if the user in not authorized.
    */
  private def onUnauthorized(request: RequestHeader) =
    Results.Redirect(lunatech.lunchplanner.controllers.routes.Authentication.login())


  def isOnWhiteList(email: String) = {
//    if (environment.mode == Mode.Prod) {
      val CONSUMER_KEY = configuration.getString("google.clientId")
      val CONSUMER_SECRET = configuration.getString("google.secret")
      val DOMAIN = configuration.getString("google.domain")

      val oauthParameters = new GoogleOAuthParameters()
      oauthParameters.setOAuthConsumerKey(CONSUMER_KEY.get)
      oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET.get)
      val signer = new OAuthHmacSha1Signer()
      val feedUrl = new URL("https://apps-apis.google.com/a/feeds/" + DOMAIN.get + "/user/2.0")

      val service = new UserService("ProvisiongApiClient")
      service.setOAuthCredentials(oauthParameters, signer)
      service.useSsl()
      val resultFeed = service.getFeed(feedUrl, classOf[UserFeed])

      import scala.collection.JavaConversions._
      val users = resultFeed.getEntries.toSet
      val filteredUsers = users.map(entry => entry.getTitle.getPlainText + "@" + DOMAIN.get)

      filteredUsers.contains(email)
//    } else {
//      true
//    }
  }

}
