package lunatech.lunchplanner.services

import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ User, UserProfile }
import lunatech.lunchplanner.persistence.{ UserProfileTable, UserTable }
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject() (configuration: Configuration, implicit val connection: DBConnection) {

  def getByEmailAddress(emailAddress: String): Future[Option[User]] = UserTable.getByEmailAddress(emailAddress)

  def isAdminUser(emailAddress: String): Boolean =
    configuration.get[Seq[String]]("administrators").contains(emailAddress)

  def addIfNew(emailAddress: String): Future[User] = {
    val name = getUserNameFromEmail(emailAddress)
    val newUser = User(name = name, emailAddress = emailAddress)

    UserTable.existsByEmail(emailAddress).flatMap(exist => {
      if (!exist) {
        UserTable.add(newUser).map(user => {
          UserProfileTable.insertOrUpdate(UserProfile(newUser.uuid))
          user }
          )
      }
      else {
        Future.successful(newUser)
      }
    })
  }

  def getAllEmailAddresses: Future[Seq[String]] = {
    UserTable.getAll.flatMap { users =>
      val emailAddresses = users.map(user => user.emailAddress)

      Future.successful(emailAddresses)
    }
  }

  private def getUserNameFromEmail(emailAddress: String) =
    emailAddress.split("@").head.split("\\.").map(w => w.head.toUpper + w.tail ).mkString(" ")
}
