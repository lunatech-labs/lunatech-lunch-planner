package lunatech.lunchplanner.services

import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.UserTable
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject() (configuration: Configuration, implicit val connection: DBConnection) {

  def getUserByEmailAddress(emailAddress: String): Future[Option[User]] = UserTable.getUserByEmailAddress(emailAddress)

  def isAdminUser(emailAddress: String): Boolean =
    configuration.getString("administrator").getOrElse("").contains(emailAddress)

  def addIfNewUser(emailAddress: String): Future[User] = {
    val name = getUserNameFromEmail(emailAddress)
    val newUser = User(name = name, emailAddress = emailAddress)

    UserTable.userWithEmailExists(emailAddress).map(exist => {
      if (!exist) UserTable.addUser(newUser)
      newUser
    })
  }

  private def getUserNameFromEmail(emailAddress: String) =
    emailAddress.split("@").head.split("\\.").map(w => w.head.toUpper + w.tail ).mkString(" ")
}
