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

class MenuPerDayTable(tag: Tag)
    extends Table[MenuPerDay](tag, _tableName = "MenuPerDay") {
  private val menuTable = TableQuery[MenuTable]
  private val dishTable = TableQuery[DishTable]

  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def menuUuid: Rep[UUID] = column[UUID]("menuUuid")

  def date: Rep[Date] = column[Date]("date")

  def location: Rep[String] = column[String]("location")

  def isDeleted: Rep[Boolean] = column[Boolean]("isDeleted")

  def menuPerDayMenuForeignKey: ForeignKeyQuery[MenuTable, Menu] =
    foreignKey(name = "menuPerDayMenu_fkey_",
               sourceColumns = menuUuid,
               targetTableQuery = menuTable)(_.uuid)

  def * : ProvenShape[MenuPerDay] =
    (uuid, menuUuid, date, location, isDeleted) <> ((MenuPerDay.apply _).tupled, MenuPerDay.unapply)
}

object MenuPerDayTable {
  val menuPerDayTable: TableQuery[MenuPerDayTable] = TableQuery[MenuPerDayTable]

  def add(menuPerDay: MenuPerDay)(
      implicit connection: DBConnection): Future[MenuPerDay] = {
    val query = menuPerDayTable returning menuPerDayTable += menuPerDay
    connection.db.run(query)
  }

  def getByUuid(uuid: UUID)(
      implicit connection: DBConnection): Future[Option[MenuPerDay]] = {
    val query = menuPerDayTable.filter(x => x.uuid === uuid)
    connection.db.run(query.result.headOption)
  }

  def getByMenuUuid(menuUuid: UUID)(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable.filter(mpd =>
      mpd.menuUuid === menuUuid && mpd.isDeleted === false)
    connection.db.run(query.result)
  }

  def getByDate(date: Date)(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable.filter(mpd =>
      mpd.date === date && mpd.isDeleted === false)
    connection.db.run(query.result)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    connection.db.run(menuPerDayTable.filter(_.isDeleted === false).result)
  }

  def getAllOrderedByDateAscending(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query =
      menuPerDayTable.filter(_.isDeleted === false).sortBy(menu => menu.date)
    connection.db.run(query.result)
  }

  def getAllFutureAndOrderedByDateAscending(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable
      .filter(mpd =>
        mpd.isDeleted === false && mpd.date >= new Date(DateTime.now.getMillis))
      .sortBy(mpd => mpd.date)
    connection.db.run(query.result)
  }

  def getAllFilteredDateRangeOrderedDateAscending(dateStart: Date,
                                                  dateEnd: Date)(
      implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable
      .filter(mpd => mpd.isDeleted === false && mpd.date >= dateStart && mpd.date <= dateEnd)
      .sortBy(mpd => mpd.date)
    connection.db.run(query.result)
  }

  // Method to be used on reports, does not filter out deleted data
  def getAllFilteredDateRangeOrderedDateAscendingWithDeleted(dateStart: Date,
    dateEnd: Date)(
    implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable
      .filter(mpd => mpd.date >= dateStart && mpd.date <= dateEnd)
      .sortBy(mpd => mpd.date)
    connection.db.run(query.result)
  }

  def removeByUuid(uuid: UUID)(
      implicit connection: DBConnection): Future[Int] = {
    val query =
      menuPerDayTable.filter(_.uuid === uuid).map(_.isDeleted).update(true)
    connection.db.run(query)
  }

  def removeByMenuUuid(menuUuid: UUID)(
      implicit connection: DBConnection): Future[Int] = {
    val query = menuPerDayTable
      .filter(_.menuUuid === menuUuid)
      .map(_.isDeleted)
      .update(true)
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
           FROM "MenuPerDay" mpd JOIN "Menu" m ON mpd."menuUuid"=m."uuid" AND m."isDeleted" = FALSE
           WHERE mpd."date" = (SELECT current_date - cast(extract(dow FROM current_date) AS int) + 5)"""
        .as[(MenuPerDay, String)]

    connection.db.run(query)
  }

  // Method used on reports, data not filtered by `isDeleted`
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
