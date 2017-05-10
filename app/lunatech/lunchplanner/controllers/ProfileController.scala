package lunatech.lunchplanner.controllers

import java.util.UUID

import com.google.inject.Inject
import lunatech.lunchplanner.models.UserProfile
import lunatech.lunchplanner.services.{ UserProfileService, UserService }
import lunatech.lunchplanner.viewModels.ProfileForm
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProfileController @Inject()(
  userService: UserService,
  userProfileService: UserProfileService,
  val environment: Environment,
  val messagesApi: MessagesApi,
  val configuration: Configuration)
  extends Controller with Secured with I18nSupport {

  def getProfile = IsAuthenticatedAsync { username =>
    implicit request => {
      for{
        currentUser <- userService.getByEmailAddress(username)
        user <- Future.successful(getCurrentUser(currentUser, isAdmin = userService.isAdminUser(currentUser.get.emailAddress), username))
        userProfile <- userProfileService.getUserProfileByUserUuid(user.uuid)
      } yield
        Ok(views.html.profile(
          user,
          ProfileForm.profileForm,
          userProfile.getOrElse(UserProfile(userUuid = user.uuid))
        ))
    }
  }

  def saveProfile = IsAuthenticatedAsync { username =>
    implicit request => {
      ProfileForm
        .profileForm
        .bindFromRequest
        .fold(
          formWithErrors => {
            for {
              currentUser <- userService.getByEmailAddress(username)
              user <- Future.successful(getCurrentUser(currentUser, isAdmin = userService.isAdminUser(currentUser.get.emailAddress), username))
              userProfile <- userProfileService.getUserProfileByUserUuid(user.uuid)
            } yield BadRequest(
              views.html.profile(
                user,
                formWithErrors,
                userProfile.getOrElse(UserProfile(userUuid = user.uuid)))
            )},
          profileForm => updateUserProfile(profileForm, username).map{ result =>
            Redirect(lunatech.lunchplanner.controllers.routes.ProfileController.getProfile)
                .flashing(
                  result match {
                    case (true) => "success" -> "Profile saved!"
                    case (false) => "error" -> "Error when saving profile!"
                  })
          })
    }
  }

  private def updateUserProfile(profile: ProfileForm, username: String): Future[Boolean] = {
    for {
      userOption <- userService.getByEmailAddress(username)
      result <- userOption.map { user =>
        userProfileService.insertOrUpdate(getUserProfile(user.uuid, profile))
      }.getOrElse(Future.successful(false))
    } yield result

  }

  private def getUserProfile(userUuid: UUID, profile: ProfileForm) =
    UserProfile(
      userUuid,
      profile.vegetarian,
      profile.seaFoodRestriction,
      profile.porkRestriction,
      profile.beefRestriction,
      profile.chickenRestriction,
      profile.glutenRestriction,
      profile.lactoseRestriction,
      profile.otherRestriction)

}
