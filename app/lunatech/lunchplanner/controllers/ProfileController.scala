package lunatech.lunchplanner.controllers

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.models.UserProfile
import lunatech.lunchplanner.services.{UserProfileService, UserService}
import lunatech.lunchplanner.viewModels.ProfileForm
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import play.api.{Configuration, Environment}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProfileController @Inject()(
    userService: UserService,
    userProfileService: UserProfileService,
    val controllerComponents: ControllerComponents,
    val environment: Environment,
    val configuration: Configuration)
    extends BaseController
    with Secured
    with I18nSupport {

  def getProfile: Action[AnyContent] = userAction.async { implicit request =>
    for {
      currentUser <- userService.getByEmailAddress(request.email)
      user <- Future.successful(
        getCurrentUser(currentUser,
                       isAdmin =
                         userService.isAdminUser(currentUser.get.emailAddress),
                       request.email))
      userProfile <- userProfileService.getUserProfileByUserUuid(user.uuid)
    } yield
      Ok(
        views.html.profile(
          user,
          ProfileForm.profileForm,
          userProfile.getOrElse(UserProfile(userUuid = user.uuid))
        ))
  }

  def saveProfile: Action[AnyContent] = userAction.async { implicit request =>
    ProfileForm.profileForm.bindFromRequest
      .fold(
        formWithErrors => {
          for {
            currentUser <- userService.getByEmailAddress(request.email)
            user <- Future.successful(
              getCurrentUser(
                currentUser,
                isAdmin = userService.isAdminUser(currentUser.get.emailAddress),
                request.email))
            userProfile <- userProfileService.getUserProfileByUserUuid(
              user.uuid)
          } yield
            BadRequest(
              views.html.profile(
                user,
                formWithErrors,
                userProfile.getOrElse(UserProfile(userUuid = user.uuid)))
            )
        },
        profileForm =>
          updateUserProfile(profileForm, request.email).map { result =>
            Redirect(
              lunatech.lunchplanner.controllers.routes.ProfileController
                .getProfile())
              .flashing(result match {
                case (true)  => "success" -> "Profile saved!"
                case (false) => "error" -> "Error when saving profile!"
              })
        }
      )
  }

  private def updateUserProfile(profile: ProfileForm,
                                username: String): Future[Boolean] = {
    for {
      userOption <- userService.getByEmailAddress(username)
      result <- userOption
        .map { user =>
          userProfileService.update(getUserProfile(user.uuid, profile))
        }
        .getOrElse(Future.successful(false))
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
      profile.otherRestriction
    )

}
