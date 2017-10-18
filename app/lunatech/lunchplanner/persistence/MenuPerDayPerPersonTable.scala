package lunatech.lunchplanner.persistence

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{MenuPerDay, MenuPerDayPerPerson, User, UserProfile}
import slick.driver.PostgresDriver.api._
import slick.jdbc.GetResult
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonTable(tag: Tag) extends Table[MenuPerDayPerPerson](tag, "MenuPerDayPerPerson") {
  private val menuPerDayTable = TableQuery[MenuPerDayTable]
  private val userTable = TableQuery[UserTable]

  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def menuPerDayUuid: Rep[UUID] = column[UUID]("menuPerDayUuid")

  def userUuid: Rep[UUID] = column[UUID]("userUuid")

  def isAttending: Rep[Boolean] = column[Boolean]("isAttending")

  def menuPerDayPerPersonMenuPerDayForeignKey: ForeignKeyQuery[MenuPerDayTable, MenuPerDay] =
    foreignKey("menuPerDayPerPersonMenuPerDay_fkey_", menuPerDayUuid, menuPerDayTable)(_.uuid)

  def menuPerDayPerPersonUserForeignKey: ForeignKeyQuery[UserTable, User] =
    foreignKey("menuPerDayPerPersonUser_fkey_", userUuid, userTable)(_.uuid)

  def * : ProvenShape[MenuPerDayPerPerson] = (uuid, menuPerDayUuid, userUuid, isAttending) <> ((MenuPerDayPerPerson.apply _).tupled, MenuPerDayPerPerson.unapply)
}

object MenuPerDayPerPersonTable {
  val menuPerDayPerPersonTable: TableQuery[MenuPerDayPerPersonTable] = TableQuery[MenuPerDayPerPersonTable]

  def add(menuPerDayPerPerson: MenuPerDayPerPerson)(implicit connection: DBConnection): Future[MenuPerDayPerPerson] = {
    val query = menuPerDayPerPersonTable returning menuPerDayPerPersonTable += menuPerDayPerPerson
    connection.db.run(query)
  }
  def exists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(menuPerDayPerPersonTable.filter(_.uuid === uuid).exists.result)
  }

  def getByUUID(uuid: UUID)(implicit connection: DBConnection): Future[Option[MenuPerDayPerPerson]] = {
    exists(uuid).flatMap {
      case true =>
        val query = menuPerDayPerPersonTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getByMenuPerDayUuid(menuPerDayUuid: UUID)(implicit connection: DBConnection): Future[Seq[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable.filter(_.menuPerDayUuid === menuPerDayUuid)
    connection.db.run(query.result)
  }

  def getAttendeesByMenuPerDayUuid(menuPerDayUuid: UUID)(implicit connection: DBConnection): Future[Seq[(User, UserProfile)]] = {
    val query = for {
      mpdpp <- menuPerDayPerPersonTable.filter(mpdppt => mpdppt.menuPerDayUuid === menuPerDayUuid && mpdppt.isAttending)
      user <- UserTable.userTable if mpdpp.userUuid === user.uuid
      userProfile <- UserProfileTable.userProfileTable if user.uuid === userProfile.userUuid
    } yield (user, userProfile)

    connection.db.run(query.result)
  }

  def getNotAttendingByDate(date: Date)(implicit connection: DBConnection): Future[Seq[(User, UserProfile)]] = {
    implicit val result = GetResult(r => (User(uuid = UUID.fromString(r.<<), name = r.<<, emailAddress = r.<<, isAdmin = r.<<),
            UserProfile(userUuid = UUID.fromString(r.<<), vegetarian = r.<<, seaFoodRestriction = r.<<, porkRestriction = r.<<, beefRestriction = r.<<,
              chickenRestriction = r.<<, glutenRestriction = r.<<, lactoseRestriction = r.<<, otherRestriction = Option(r.<<)
            )
          ))

    val query = sql"""SELECT DISTINCT ON (u."uuid") u.*, up.*
                            FROM "MenuPerDayPerPerson" mpdpp
                            JOIN "MenuPerDay" mpd ON mpdpp."menuPerDayUuid" = mpd."uuid"
                            JOIN "User" u ON mpdpp."userUuid" = u."uuid"
                            JOIN "UserProfile" up ON u."uuid" = up."userUuid"
                            WHERE mpdpp."isAttending" = FALSE AND mpd."date" = '#$date'""".as[(User, UserProfile)]

    connection.db.run(query)
  }

  def getAllUpcomingSchedulesByUser(userUuid: UUID)(implicit connection: DBConnection) : Future[Seq[MenuPerDayPerPerson]] = {
    implicit val getMenuPerDayPerPersonResult = GetResult(r =>
      MenuPerDayPerPerson(UUID.fromString(r.<<), UUID.fromString(r.<<), UUID.fromString(r.<<), r.<<)
    )
    val query = sql"""SELECT mpdpp."uuid", mpdpp."menuPerDayUuid", mpdpp."userUuid", mpdpp."isAttending"
                            FROM "MenuPerDayPerPerson" mpdpp
                            JOIN "MenuPerDay" mpd ON mpdpp."menuPerDayUuid" = mpd."uuid"
                            WHERE mpdpp."userUuid"='#$userUuid'
                            AND mpd."date" >= current_date""".as[MenuPerDayPerPerson]

    connection.db.run(query)
  }

  def getByUserUuid(userUuid: UUID)(implicit connection: DBConnection): Future[Seq[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable.filter(_.userUuid === userUuid)
    connection.db.run(query.result)
  }

  def getByUserUuidAndMenuPerDayUuid(userUuid: UUID, menuPerDayUuid: UUID)(implicit connection: DBConnection): Future[Option[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable.filter(_.userUuid === userUuid).filter(_.menuPerDayUuid === menuPerDayUuid)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[MenuPerDayPerPerson]] = {
    connection.db.run(menuPerDayPerPersonTable.result)
  }

  def remove(uuid: UUID)(implicit connection: DBConnection): Future[Int]  = {
    val query = menuPerDayPerPersonTable.filter(x => x.uuid === uuid).delete
    connection.db.run(query)
  }

  def removeByMenuPerDayUuid(menuPerDayUuid: UUID)(implicit connection: DBConnection): Future[Int] = {
    val query = menuPerDayPerPersonTable.filter(x => x.menuPerDayUuid === menuPerDayUuid).delete
    connection.db.run(query)
  }
}

