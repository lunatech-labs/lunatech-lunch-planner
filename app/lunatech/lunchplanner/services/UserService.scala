package lunatech.lunchplanner.services

import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.UserTable
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject() (configuration: Configuration, implicit val connection: DBConnection) {

  def getByEmailAddress(emailAddress: String): Future[Option[User]] = UserTable.getByEmailAddress(emailAddress)

  def isAdminUser(emailAddress: String): Boolean =
    configuration.getStringList("administrators").get.contains(emailAddress)

  def addIfNew(emailAddress: String): Future[User] = {
    val name = getUserNameFromEmail(emailAddress)
    val newUser = User(name = name, emailAddress = emailAddress)

    UserTable.existsByEmail(emailAddress).map(exist => {
      if (!exist) UserTable.add(newUser)
      newUser
    })
  }

  private def getUserNameFromEmail(emailAddress: String) =
    emailAddress.split("@").head.split("\\.").map(w => w.head.toUpper + w.tail ).mkString(" ")
}
