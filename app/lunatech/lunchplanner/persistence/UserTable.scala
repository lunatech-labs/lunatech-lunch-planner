package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.User
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, TableQuery}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserTable(tag: Tag) extends Table[User](tag, _tableName = "User") {
  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def name: Rep[String] = column[String]("name")

  def emailAddress: Rep[String] = column[String]("emailAddress")

  def isAdmin: Rep[Boolean] = column[Boolean]("isAdmin")

  def isDeleted: Rep[Boolean] = column[Boolean]("isDeleted")

  def * : ProvenShape[User] =
    (
      uuid,
      name,
      emailAddress,
      isAdmin,
      isDeleted
    ) <> ((User.apply _).tupled, User.unapply)
}

object UserTable {
  val userTable: TableQuery[UserTable] = TableQuery[UserTable]

  def add(user: User)(implicit connection: DBConnection): Future[User] = {
    val query = userTable += user
    connection.db.run(query).map(_ => user)
  }

  def existsByEmail(
      emailAddress: String
  )(implicit connection: DBConnection): Future[Boolean] =
    connection.db.run(
      userTable
        .filter(_.emailAddress === emailAddress)
        .exists
        .result
    )

  def getByUUID(
      uuid: UUID
  )(implicit connection: DBConnection): Future[Option[User]] = {
    val query = userTable.filter(_.uuid === uuid)
    connection.db.run(query.result.headOption)
  }

  def getByEmailAddress(
      emailAddress: String
  )(implicit connection: DBConnection): Future[Option[User]] = {
    val query = userTable.filter(_.emailAddress === emailAddress)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[User]] =
    connection.db.run(userTable.filter(_.isDeleted === false).result)

  def removeByUuid(
      uuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query = userTable.filter(_.uuid === uuid).map(_.isDeleted).update(true)
    connection.db.run(query)
  }
}
