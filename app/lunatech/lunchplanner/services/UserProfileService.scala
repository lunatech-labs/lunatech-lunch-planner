package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.UserProfile
import lunatech.lunchplanner.persistence.UserProfileTable
import play.api.Configuration

import scala.concurrent.Future

class UserProfileService @Inject() (configuration: Configuration, implicit val connection: DBConnection) {

  def insertOrUpdate(userProfile: UserProfile): Future[Boolean] =
    UserProfileTable.insertOrUpdate(userProfile)

  def getUserProfileBtUserUuid(userUuid: UUID): Future[Option[UserProfile]] =
    UserProfileTable.getByUserUUID(userUuid)
}
