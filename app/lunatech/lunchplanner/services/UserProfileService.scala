package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDayDietRestrictions, UserProfile }
import lunatech.lunchplanner.persistence.UserProfileTable
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserProfileService @Inject() (configuration: Configuration, implicit val connection: DBConnection) {

  def insertOrUpdate(userProfile: UserProfile): Future[Boolean] =
    UserProfileTable.insertOrUpdate(userProfile)

  def getUserProfileByUserUuid(userUuid: UUID): Future[Option[UserProfile]] =
    UserProfileTable.getByUserUUID(userUuid)

  def getRestrictionsByMenuPerDay(menuPerDayUuid: UUID): Future[MenuPerDayDietRestrictions] =
    UserProfileTable.getRestrictionsByMenuPerDay(menuPerDayUuid).map(res =>
      res.map(count =>
        MenuPerDayDietRestrictions(menuPerDayUuid, count._1, count._2, count._3, count._4, count._5, count._6, count._7)).head)
}
