package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import slick.driver.PostgresDriver.api._
import slick.lifted.{ ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserTable(tag: Tag) extends Table[User](tag, "User") {
  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def name: Rep[String] = column[String]("name")

  def emailAddress: Rep[String] = column[String]("emailAddress")

  def isAdmin: Rep[Boolean] = column[Boolean]("isAdmin")

  def * : ProvenShape[User] = (uuid, name, emailAddress, isAdmin) <> ((User.apply _).tupled, User.unapply)
}

object UserTable {
  val userTable: TableQuery[UserTable] = TableQuery[UserTable]

  def addUser(user: User)(implicit connection: DBConnection): Future[User] = {
    val query = userTable returning userTable += user
    connection.db.run(query)
  }

  def userExists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(userTable.filter(_.uuid === uuid).exists.result)
  }

  def getUserByUUID(uuid: UUID)(implicit connection: DBConnection): Future[Option[User]] = {
    userExists(uuid).flatMap {
      case true =>
        val query = userTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getUserByEmailAddress(emailAddress: String)(implicit connection: DBConnection): Future[Option[User]] = {
    val query = userTable.filter(_.emailAddress === emailAddress)
    connection.db.run(query.result.headOption)
  }

  def getAllUsers(implicit connection: DBConnection): Future[Seq[User]] = {
    connection.db.run(userTable.result)
  }

  def removeUser(uuid: UUID)(implicit connection: DBConnection): Future[Int]  = {
    userExists(uuid).flatMap {
      case true =>
        val query = userTable.filter(x => x.uuid === uuid).delete
        connection.db.run(query)
      case false => Future(0)
    }
  }

  def isAdminUser(userEmailAddress: String)(implicit connection: DBConnection): Future[Boolean] =
    getUserByEmailAddress(userEmailAddress)
      .map(user => user.map(_.isAdmin))
      .map(_.getOrElse(false))

}
