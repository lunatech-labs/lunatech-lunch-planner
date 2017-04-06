package lunatech.lunchplanner.services

import javax.inject.{ Inject, Singleton }

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.UserTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject() (implicit val connection: DBConnection) {

  def getUserByEmailAddress(emailAddress: String): Future[Option[User]] = UserTable.getUserByEmailAddress(emailAddress)

  def isAdminUser(emailAddress: String): Future[Boolean] =
    UserTable.getUserByEmailAddress(emailAddress)
    .map(user => user.map(_.isAdmin))
    .map(_.getOrElse(false))
}
