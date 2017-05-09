package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.UserProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.{ ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserProfileTable(tag: Tag) extends Table[UserProfile](tag, "UserProfile") {
  def userUuid: Rep[UUID] = column[UUID]("userUuid", O.PrimaryKey)

  def vegetarian: Rep[Boolean] = column[Boolean]("vegetarian")

  def seaFoodRestriction: Rep[Boolean] = column[Boolean]("seaFoodRestriction")

  def porkRestriction: Rep[Boolean] = column[Boolean]("porkRestriction")

  def beefRestriction: Rep[Boolean] = column[Boolean]("beefRestriction")

  def chickenRestriction: Rep[Boolean] = column[Boolean]("chickenRestriction")

  def glutenRestriction: Rep[Boolean] = column[Boolean]("glutenRestriction")

  def lactoseRestriction: Rep[Boolean] = column[Boolean]("lactoseRestriction")

  def otherRestriction: Rep[String] = column[String]("otherRestriction")

  def * : ProvenShape[UserProfile] =
    (userUuid, vegetarian, seaFoodRestriction, porkRestriction, beefRestriction, chickenRestriction, glutenRestriction, lactoseRestriction, otherRestriction.?) <> ((UserProfile.apply _).tupled, UserProfile.unapply)
}

object UserProfileTable {
  val userProfileTable: TableQuery[UserProfileTable] = TableQuery[UserProfileTable]

  def getByUserUUID(userUuid: UUID)(implicit connection: DBConnection): Future[Option[UserProfile]] = {
    val query = userProfileTable.filter(x => x.userUuid === userUuid)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[UserProfile]] = {
    connection.db.run(userProfileTable.result)
  }

  def remove(userUuid: UUID)(implicit connection: DBConnection): Future[Int] = {
    val query = userProfileTable.filter(x => x.userUuid === userUuid).delete
    connection.db.run(query)
  }

  def insertOrUpdate(userProfile: UserProfile)(implicit connection: DBConnection): Future[Boolean] = {
    val query = userProfileTable.insertOrUpdate(userProfile)
    connection.db.run(query).map(_ == 1)
  }
}

