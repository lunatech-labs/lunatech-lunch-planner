package lunatech.lunchplanner.services

import javax.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{User, UserProfile}
import lunatech.lunchplanner.persistence.{UserProfileTable, UserTable}
import play.api.Configuration
import scalaz.\/

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject()(configuration: Configuration)(
    implicit val connection: DBConnection) {

  def getByEmailAddress(emailAddress: String): Future[Option[User]] =
    UserTable.getByEmailAddress(emailAddress)

  def getByEmailAddressT(emailAddress: String): Future[\/[String, User]] = {
    for {
      user <- UserTable.getByEmailAddress(emailAddress)
    } yield {
      \/.fromEither(Either.cond(user.isDefined, user.get, s"No user found for $emailAddress"))
    }
  }

  def isAdminUser(emailAddress: String): Boolean =
    configuration.get[Seq[String]]("administrators").contains(emailAddress)

  def addUserIfNew(emailAddress: String): Future[Boolean] = {
    val name = getUserNameFromEmail(emailAddress)
    val newUser = User(name = name, emailAddress = emailAddress)

    UserTable
      .existsByEmail(emailAddress)
      .flatMap(exist => {
        if (!exist) {
          UserTable
            .add(newUser)
            .map(user => {
              UserProfileTable.insertOrUpdate(UserProfile(newUser.uuid))
              true
            })
        } else {
          Future.successful(false)
        }
      })
  }

  def getAllEmailAddresses: Future[Seq[String]] = {
    UserTable.getAll.map { users =>
      users.map(user => user.emailAddress)
    }
  }

  private[services] def getUserNameFromEmail(emailAddress: String) =
    emailAddress
      .split("@")
      .head
      .split("\\.")
      .map(w => w.head.toUpper + w.tail)
      .mkString(" ")
}
