package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import slick.driver.PostgresDriver.api._
import slick.lifted.{ ProvenShape, TableQuery }

import scala.concurrent.Future

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)
  def name: Rep[String] = column[String]("name")
  def emailAddress: Rep[String] = column[String]("emailAddress")
  def isAdmin: Rep[Boolean] = column[Boolean]("isAdmin")

  def * : ProvenShape[User] = (uuid.?, name, emailAddress, isAdmin) <> ((User.apply _).tupled, User.unapply)
}

object UserTable {
  val userTable: TableQuery[UserTable] = TableQuery[UserTable]

  def getUserByUUID(userUUID: Int)(implicit connection: DBConnection): Future[Option[User]] = {
    exists(userUUID).flatMap {
      case true =>
        val query = userTable.filter(x => x.uuid === userUUID)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getUserByEmail(emailAddress: String)(implicit connection: DBConnection): Future[Option[User]] = {
    val query = userTable.filter(_.emailAddress === emailAddress)
    connection.db.run(query.result.headOption)
  }

  def getAllUsers(implicit connection: DBConnection): Future[Seq[User]] = {
    connection.db.run(userTable.result)
  }

  def exists(id: Int)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(userTable.filter(_.uuid === id).exists.result)
  }

  def add(user: User)(implicit connection: DBConnection): Future[User] = {
    val query = userTable returning userTable += user
    connection.db.run(query)
  }
}
