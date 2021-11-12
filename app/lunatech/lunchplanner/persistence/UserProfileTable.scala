package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{User, UserProfile}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserProfileTable(tag: Tag)
    extends Table[UserProfile](tag, _tableName = "UserProfile") {
  private val userTable = TableQuery[UserTable]

  def userUuid: Rep[UUID] = column[UUID]("userUuid", O.PrimaryKey)

  def vegetarian: Rep[Boolean] = column[Boolean]("vegetarian")

  def halal: Rep[Boolean] = column[Boolean]("halal")

  def seaFoodRestriction: Rep[Boolean] = column[Boolean]("seaFoodRestriction")

  def porkRestriction: Rep[Boolean] = column[Boolean]("porkRestriction")

  def beefRestriction: Rep[Boolean] = column[Boolean]("beefRestriction")

  def chickenRestriction: Rep[Boolean] = column[Boolean]("chickenRestriction")

  def glutenRestriction: Rep[Boolean] = column[Boolean]("glutenRestriction")

  def lactoseRestriction: Rep[Boolean] = column[Boolean]("lactoseRestriction")

  def otherRestriction: Rep[Option[String]] =
    column[Option[String]]("otherRestriction")

  def isDeleted: Rep[Boolean] = column[Boolean]("isDeleted")

  def userProfileUserForeignKey: ForeignKeyQuery[UserTable, User] =
    foreignKey(
      name = "userProfileUser_fkey_",
      sourceColumns = userUuid,
      targetTableQuery = userTable
    )(_.uuid)

  def * : ProvenShape[UserProfile] =
    (
      userUuid,
      vegetarian,
      halal,
      seaFoodRestriction,
      porkRestriction,
      beefRestriction,
      chickenRestriction,
      glutenRestriction,
      lactoseRestriction,
      otherRestriction,
      isDeleted
    ) <> ((UserProfile.apply _).tupled, UserProfile.unapply)
}

object UserProfileTable {
  val userProfileTable: TableQuery[UserProfileTable] =
    TableQuery[UserProfileTable]

  def add(
      userProfile: UserProfile
  )(implicit connection: DBConnection): Future[UserProfile] = {
    val query = userProfileTable += userProfile
    connection.db.run(query).map(_ => userProfile)
  }

  def getByUserUUID(
      userUuid: UUID
  )(implicit connection: DBConnection): Future[Option[UserProfile]] = {
    val query = userProfileTable.filter(_.userUuid === userUuid)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[UserProfile]] =
    connection.db.run(userProfileTable.result)

  def removeByUserUuid(
      userUuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query = userProfileTable
      .filter(_.userUuid === userUuid)
      .map(_.isDeleted)
      .update(true)
    connection.db.run(query)
  }

  def update(
      userProfile: UserProfile
  )(implicit connection: DBConnection): Future[Boolean] = {
    val query = userProfileTable
      .filter(_.userUuid === userProfile.userUuid)
      .map(up =>
        (
          up.vegetarian,
          up.halal,
          up.seaFoodRestriction,
          up.porkRestriction,
          up.beefRestriction,
          up.chickenRestriction,
          up.glutenRestriction,
          up.lactoseRestriction,
          up.otherRestriction,
          up.isDeleted
        )
      )
      .update(
        (
          userProfile.vegetarian,
          userProfile.halal,
          userProfile.seaFoodRestriction,
          userProfile.porkRestriction,
          userProfile.beefRestriction,
          userProfile.chickenRestriction,
          userProfile.glutenRestriction,
          userProfile.lactoseRestriction,
          userProfile.otherRestriction,
          userProfile.isDeleted
        )
      )
    connection.db.run(query).map(_ == 1)
  }

  def getRestrictionsByMenuPerDay(menuPerDayUuid: UUID)(implicit
      connection: DBConnection
  ): Future[Vector[(Int, Int, Int, Int, Int, Int, Int, Int)]] = {
    val query = sql"""SELECT
         SUM(case when vegetarian then 1 else 0 end) as "vegetarianCount",
         SUM(case when halal then 1 else 0 end) as "halalCount",
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
      .as[(Int, Int, Int, Int, Int, Int, Int, Int)]

    connection.db.run(query)
  }
}
