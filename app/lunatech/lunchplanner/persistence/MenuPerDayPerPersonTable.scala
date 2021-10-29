package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{
  MenuPerDay,
  MenuPerDayPerPerson,
  User,
  UserProfile
}
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import java.sql.Date
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonTable(tag: Tag)
    extends Table[MenuPerDayPerPerson](
      tag,
      _tableName = "MenuPerDayPerPerson"
    ) {
  private val menuPerDayTable = TableQuery[MenuPerDayTable]
  private val userTable       = TableQuery[UserTable]

  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def menuPerDayUuid: Rep[UUID] = column[UUID]("menuPerDayUuid")

  def userUuid: Rep[UUID] = column[UUID]("userUuid")

  def isAttending: Rep[Boolean] = column[Boolean]("isAttending")

  def menuPerDayPerPersonMenuPerDayForeignKey
      : ForeignKeyQuery[MenuPerDayTable, MenuPerDay] =
    foreignKey(
      name = "menuPerDayPerPersonMenuPerDay_fkey_",
      sourceColumns = menuPerDayUuid,
      targetTableQuery = menuPerDayTable
    )(_.uuid)

  def menuPerDayPerPersonUserForeignKey: ForeignKeyQuery[UserTable, User] =
    foreignKey(
      name = "menuPerDayPerPersonUser_fkey_",
      sourceColumns = userUuid,
      targetTableQuery = userTable
    )(_.uuid)

  def * : ProvenShape[MenuPerDayPerPerson] =
    (
      uuid,
      menuPerDayUuid,
      userUuid,
      isAttending
    ) <> ((MenuPerDayPerPerson.apply _).tupled, MenuPerDayPerPerson.unapply)
}

object MenuPerDayPerPersonTable {
  val menuPerDayPerPersonTable: TableQuery[MenuPerDayPerPersonTable] =
    TableQuery[MenuPerDayPerPersonTable]

  def addOrUpdate(menuPerDayPerPerson: MenuPerDayPerPerson)(implicit
      connection: DBConnection
  ): Future[MenuPerDayPerPerson] =
    for {
      containsData <- contains(menuPerDayPerPerson)
      result <-
        if (containsData.isEmpty) add(menuPerDayPerPerson)
        else update(menuPerDayPerPerson)
    } yield result

  private def contains(
      mpdpp: MenuPerDayPerPerson
  )(implicit connection: DBConnection): Future[Option[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable.filter(mpdppt =>
      mpdppt.menuPerDayUuid === mpdpp.menuPerDayUuid && mpdppt.userUuid === mpdpp.menuPerDayUuid
    )
    connection.db.run(query.result.headOption)
  }

  private def add(
      menuPerDayPerPerson: MenuPerDayPerPerson
  )(implicit connection: DBConnection): Future[MenuPerDayPerPerson] = {
    val query = menuPerDayPerPersonTable += menuPerDayPerPerson
    connection.db.run(query).map(_ => menuPerDayPerPerson)
  }

  private def update(
      mpdpp: MenuPerDayPerPerson
  )(implicit connection: DBConnection): Future[MenuPerDayPerPerson] = {
    val query = menuPerDayPerPersonTable
      .filter(mpdppt =>
        mpdppt.menuPerDayUuid === mpdpp.menuPerDayUuid && mpdppt.userUuid === mpdpp.menuPerDayUuid
      )
      .map(mp => mp.isAttending)
      .update(mpdpp.isAttending)

    connection.db.run(query).map(_ => mpdpp)
  }

  def getByUuid(
      uuid: UUID
  )(implicit connection: DBConnection): Future[Option[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable.filter(_.uuid === uuid)
    connection.db.run(query.result.headOption)
  }

  def getByMenuPerDayUuid(
      menuPerDayUuid: UUID
  )(implicit connection: DBConnection): Future[Seq[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable.filter(mpdpp =>
      mpdpp.menuPerDayUuid === menuPerDayUuid
    )
    connection.db.run(query.result)
  }

  def getAttendeeCountByMenuPerDayUuid(
      menuPerDayUuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query = menuPerDayPerPersonTable
      .filter(mpdpp => mpdpp.menuPerDayUuid === menuPerDayUuid)
      .filter(_.isAttending === true)
      .length
    connection.db.run(query.result)
  }

  def getAttendeesByMenuPerDayUuid(
      menuPerDayUuid: UUID
  )(implicit connection: DBConnection): Future[Seq[(User, UserProfile)]] = {
    val query = {
      for {
        mpdpp <- menuPerDayPerPersonTable.filter(mpdppt =>
          mpdppt.menuPerDayUuid === menuPerDayUuid && mpdppt.isAttending
        )
        user        <- UserTable.userTable if mpdpp.userUuid === user.uuid
        userProfile <- UserProfileTable.userProfileTable
        if user.uuid === userProfile.userUuid
      } yield (user, userProfile)
    }.distinctOn { case (user, _) =>
      user.name
    }

    connection.db.run(query.result)
  }

  def getNotAttendingByDate(
      date: Date
  )(implicit connection: DBConnection): Future[Seq[User]] = {
    implicit val result = GetResult(r =>
      User(
        uuid = UUID.fromString(r.<<),
        name = r.<<,
        emailAddress = r.<<,
        isAdmin = r.<<
      )
    )

    val query = sql"""SELECT u.*
                            FROM "MenuPerDayPerPerson" mpdpp
                            JOIN "MenuPerDay" mpd ON mpdpp."menuPerDayUuid" = mpd."uuid"
                            JOIN "User" u ON mpdpp."userUuid" = u."uuid"
                            WHERE mpdpp."isAttending" = FALSE AND mpd."date" = '#$date'
                            GROUP BY u."uuid"
                            ORDER BY u."uuid""""
      .as[User]

    connection.db.run(query)
  }

  def getAllUpcomingSchedulesByUser(
      userUuid: UUID
  )(implicit connection: DBConnection): Future[Seq[MenuPerDayPerPerson]] = {
    implicit val getMenuPerDayPerPersonResult = GetResult(r =>
      MenuPerDayPerPerson(
        UUID.fromString(r.<<),
        UUID.fromString(r.<<),
        UUID.fromString(r.<<),
        r.<<
      )
    )
    val query =
      sql"""SELECT mpdpp."uuid", mpdpp."menuPerDayUuid", mpdpp."userUuid", mpdpp."isAttending"
                            FROM "MenuPerDayPerPerson" mpdpp
                            JOIN "MenuPerDay" mpd ON mpdpp."menuPerDayUuid" = mpd."uuid" AND mpd."isDeleted" = FALSE
                            WHERE mpdpp."userUuid"='#$userUuid'
                            AND mpd."date" >= current_date"""
        .as[MenuPerDayPerPerson]

    connection.db.run(query)
  }

  def getByUserUuid(
      userUuid: UUID
  )(implicit connection: DBConnection): Future[Seq[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable.filter(_.userUuid === userUuid)
    connection.db.run(query.result)
  }

  def getByUserUuidAndMenuPerDayUuid(userUuid: UUID, menuPerDayUuid: UUID)(
      implicit connection: DBConnection
  ): Future[Option[MenuPerDayPerPerson]] = {
    val query = menuPerDayPerPersonTable
      .filter(_.userUuid === userUuid)
      .filter(_.menuPerDayUuid === menuPerDayUuid)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit
      connection: DBConnection
  ): Future[Seq[MenuPerDayPerPerson]] =
    connection.db.run(menuPerDayPerPersonTable.result)

  def removeByUuid(
      uuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query = menuPerDayPerPersonTable.filter(_.uuid === uuid).delete
    connection.db.run(query)
  }

  def removeByMenuPerDayUuid(
      menuPerDayUuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query = menuPerDayPerPersonTable
      .filter(_.menuPerDayUuid === menuPerDayUuid)
      .delete
    connection.db.run(query)
  }

  /** Get attendees for upcoming Friday lunch. The '5' refers to Friday.
    */
  def getAttendeesEmailAddressesForUpcomingLunch(implicit
      connection: DBConnection
  ): Future[Seq[String]] = {
    val query = sql"""SELECT u."emailAddress" FROM "MenuPerDayPerPerson" mpdpp
                      JOIN "User" u ON mpdpp."userUuid"=u."uuid" AND u."isDeleted" = FALSE
                      JOIN "MenuPerDay" mpd ON mpdpp."menuPerDayUuid"=mpd."uuid" AND mpd."isDeleted" = FALSE
                      WHERE mpd."date" = (SELECT current_date - cast(extract(dow FROM current_date) AS int) + 5)"""
      .as[String]
    connection.db.run(query)
  }
}
