package lunatech.lunchplanner.persistence

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{Menu, MenuPerDay}
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.GetResult
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayTable(tag: Tag) extends Table[MenuPerDay](tag, "MenuPerDay") {
  private val menuTable = TableQuery[MenuTable]
  private val dishTable = TableQuery[DishTable]

  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def menuUuid: Rep[UUID] = column[UUID]("menuUuid")

  def date: Rep[Date] = column[Date]("date")

  def location: Rep[String] = column[String]("location")

  def menuPerDayMenuForeignKey: ForeignKeyQuery[MenuTable, Menu] =
    foreignKey("menuPerDayMenu_fkey_", menuUuid, menuTable)(_.uuid)

  def * : ProvenShape[MenuPerDay] =
    (uuid, menuUuid, date, location) <> ((MenuPerDay.apply _).tupled, MenuPerDay.unapply)
}

object MenuPerDayTable {
  val menuPerDayTable: TableQuery[MenuPerDayTable] = TableQuery[MenuPerDayTable]

  def add(menuPerDay: MenuPerDay)(
      implicit connection: DBConnection): Future[MenuPerDay] = {
    val query = menuPerDayTable returning menuPerDayTable += menuPerDay
    connection.db.run(query)
  }

  def exists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(menuPerDayTable.filter(_.uuid === uuid).exists.result)
  }

  def getByUuid(uuid: UUID)(
      implicit connection: DBConnection): Future[Option[MenuPerDay]] = {
    exists(uuid).flatMap {
      case true =>
        val query = menuPerDayTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getByMenuUuid(menuUuid: UUID)(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable.filter(_.menuUuid === menuUuid)
    connection.db.run(query.result)
  }

  def getByDate(date: Date)(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable.filter(_.date === date)
    connection.db.run(query.result)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    connection.db.run(menuPerDayTable.result)
  }

  def getAllOrderedByDateAscending(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable.sortBy(menu => menu.date)
    connection.db.run(query.result)
  }

  def getAllFutureAndOrderedByDateAscending(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable
      .filter(menu => menu.date >= new Date(DateTime.now.getMillis))
      .sortBy(menu => menu.date)
    connection.db.run(query.result)
  }

  def getAllFilteredDateRangeOrderedDateAscending(dateStart: Date,
                                                  dateEnd: Date)(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable
      .filter(menu => menu.date >= dateStart && menu.date <= dateEnd)
      .sortBy(menu => menu.date)
    connection.db.run(query.result)
  }

  def removeByUuid(uuid: UUID)(
      implicit connection: DBConnection): Future[Int] = {
    val query = menuPerDayTable.filter(x => x.uuid === uuid).delete
    connection.db.run(query)
  }

  def removeByMenuUuid(menuUuid: UUID)(
      implicit connection: DBConnection): Future[Int] = {
    val query = menuPerDayTable.filter(x => x.menuUuid === menuUuid).delete
    connection.db.run(query)
  }

  def insertOrUpdate(menuPerDay: MenuPerDay)(
      implicit connection: DBConnection): Future[Boolean] = {
    val query = menuPerDayTable.insertOrUpdate(menuPerDay)
    connection.db.run(query).map(_ == 1)
  }

  def getMenuForUpcomingSchedule(
      implicit connection: DBConnection): Future[Seq[(MenuPerDay, String)]] = {
    implicit val getTupleResult: GetResult[MenuPerDay] = GetResult(
      r => MenuPerDay(UUID.fromString(r.<<), UUID.fromString(r.<<), r.<<, r.<<))
    val query =
      sql"""SELECT mpd."uuid", mpd."menuUuid", mpd."date", mpd."location", m."name"
           FROM "MenuPerDay" mpd JOIN "Menu" m ON mpd."menuUuid"=m."uuid"
           WHERE mpd."date" = (SELECT current_date - cast(extract(dow FROM current_date) AS int) + 5)"""
        .as[(MenuPerDay, String)]

    connection.db.run(query)
  }

  def getAllAvailableDatesWithinRange(dateStart: Date, dateEnd: Date)(
      implicit connection: DBConnection): Future[Seq[Date]] = {
    implicit val result: GetResult[Date] = GetResult(r => r.nextDate)

    val query = sql"""SELECT DISTINCT "date"
                            FROM "MenuPerDay"
                            WHERE "date" >= '#$dateStart' AND "date" <= '#$dateEnd'"""
      .as[Date]
    connection.db.run(query)
  }
}
