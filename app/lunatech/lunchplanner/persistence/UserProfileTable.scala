package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{User, UserProfile}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserProfileTable(tag: Tag)
    extends Table[UserProfile](tag, "UserProfile") {
  private val userTable = TableQuery[UserTable]

  def userUuid: Rep[UUID] = column[UUID]("userUuid", O.PrimaryKey)

  def vegetarian: Rep[Boolean] = column[Boolean]("vegetarian")

  def seaFoodRestriction: Rep[Boolean] = column[Boolean]("seaFoodRestriction")

  def porkRestriction: Rep[Boolean] = column[Boolean]("porkRestriction")

  def beefRestriction: Rep[Boolean] = column[Boolean]("beefRestriction")

  def chickenRestriction: Rep[Boolean] = column[Boolean]("chickenRestriction")

  def glutenRestriction: Rep[Boolean] = column[Boolean]("glutenRestriction")

  def lactoseRestriction: Rep[Boolean] = column[Boolean]("lactoseRestriction")

  def otherRestriction: Rep[String] = column[String]("otherRestriction")

  def userProfileUserForeignKey: ForeignKeyQuery[UserTable, User] =
    foreignKey("userProfileUser_fkey_", userUuid, userTable)(_.uuid)

  def * : ProvenShape[UserProfile] =
    (userUuid,
     vegetarian,
     seaFoodRestriction,
     porkRestriction,
     beefRestriction,
     chickenRestriction,
     glutenRestriction,
     lactoseRestriction,
     otherRestriction.?) <> ((UserProfile.apply _).tupled, UserProfile.unapply)
}

object UserProfileTable {
  val userProfileTable: TableQuery[UserProfileTable] =
    TableQuery[UserProfileTable]

  def getByUserUUID(userUuid: UUID)(
      implicit connection: DBConnection): Future[Option[UserProfile]] = {
    val query = userProfileTable.filter(x => x.userUuid === userUuid)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[UserProfile]] =
    connection.db.run(userProfileTable.result)

  def removeByUserUuid(userUuid: UUID)(
      implicit connection: DBConnection): Future[Int] = {
    val query = userProfileTable.filter(x => x.userUuid === userUuid).delete
    connection.db.run(query)
  }

  def insertOrUpdate(userProfile: UserProfile)(
      implicit connection: DBConnection): Future[Boolean] = {
    val query = userProfileTable.insertOrUpdate(userProfile)
    connection.db.run(query).map(_ == 1)
  }

  def getRestrictionsByMenuPerDay(menuPerDayUuid: UUID)(
      implicit connection: DBConnection)
    : Future[Vector[(Int, Int, Int, Int, Int, Int, Int)]] = {
    val query = sql"""SELECT
         SUM(case when vegetarian then 1 else 0 end) as "vegetarianCount",
         SUM(case when "seaFoodRestriction" then 1 else 0 end) as "seaFoodCount",
         SUM(case when "porkRestriction" then 1 else 0 end) as "porkCount",
         SUM(case when "beefRestriction" then 1 else 0 end) as "beefCount",
         SUM(case when "chickenRestriction" then 1 else 0 end) as "chickenCount",
         SUM(case when "glutenRestriction" then 1 else 0 end) as "glutenCount",
         SUM(case when "lactoseRestriction" then 1 else 0 end) as "lactoseCount"
         FROM "MenuPerDayPerPerson" mpd
         JOIN "UserProfile" up on mpd."userUuid" = up."userUuid"
         WHERE mpd."menuPerDayUuid" = '#$menuPerDayUuid'
         GROUP BY mpd."menuPerDayUuid""""
      .as[(Int, Int, Int, Int, Int, Int, Int)]

    connection.db.run(query)
  }
}
