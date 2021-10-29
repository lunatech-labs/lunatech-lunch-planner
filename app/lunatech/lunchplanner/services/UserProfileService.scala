package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{MenuPerDayDietRestrictions, UserProfile}
import lunatech.lunchplanner.persistence.UserProfileTable

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserProfileService @Inject() ()(implicit val connection: DBConnection) {

  def add(userProfile: UserProfile): Future[UserProfile] =
    UserProfileTable.add(userProfile)

  def update(userProfile: UserProfile): Future[Boolean] =
    UserProfileTable.update(userProfile)

  def getUserProfileByUserUuid(userUuid: UUID): Future[Option[UserProfile]] =
    UserProfileTable.getByUserUUID(userUuid)

  def getRestrictionsByMenuPerDay(
      menuPerDayUuid: UUID
  ): Future[MenuPerDayDietRestrictions] =
    UserProfileTable
      .getRestrictionsByMenuPerDay(menuPerDayUuid)
      .map(res =>
        res
          .map(count =>
            MenuPerDayDietRestrictions(
              menuPerDayUuid,
              count._1,
              count._2,
              count._3,
              count._4,
              count._5,
              count._6,
              count._7,
              count._8
            )
          )
          .headOption
          .getOrElse(MenuPerDayDietRestrictions(menuPerDayUuid))
      )
}
