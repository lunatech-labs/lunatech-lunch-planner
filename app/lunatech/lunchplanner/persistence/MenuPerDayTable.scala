package lunatech.lunchplanner.persistence

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Menu, MenuPerDay }
import slick.driver.PostgresDriver.api._
import slick.lifted.{ ForeignKeyQuery, ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayTable(tag: Tag) extends Table[MenuPerDay](tag, "MenuPerDay") {
  private val menuTable = TableQuery[MenuTable]
  private val dishTable = TableQuery[DishTable]

  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def menuUuid: Rep[UUID] = column[UUID]("menuUuid")

  def date: Rep[Date] = column[Date]("date")

  def menuPerDayMenuForeignKey: ForeignKeyQuery[MenuTable, Menu] = foreignKey("menuPerDayMenu_fkey_", menuUuid, menuTable)(_.uuid)

  def * : ProvenShape[MenuPerDay] = (uuid, menuUuid, date) <> ((MenuPerDay.apply _).tupled, MenuPerDay.unapply)
}

object MenuPerDayTable {
  val menuPerDayTable: TableQuery[MenuPerDayTable] = TableQuery[MenuPerDayTable]

  def addMenuPerDay(menuPerDay: MenuPerDay)(implicit connection: DBConnection): Future[MenuPerDay] = {
    val query = menuPerDayTable returning menuPerDayTable += menuPerDay
    connection.db.run(query)
  }

  def menuPerDayExists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(menuPerDayTable.filter(_.uuid === uuid).exists.result)
  }

  def getMenuPerDayByUUID(uuid: UUID)(implicit connection: DBConnection): Future[Option[MenuPerDay]] = {
    menuPerDayExists(uuid).flatMap {
      case true =>
        val query = menuPerDayTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getMenuPerDayByMenuUuid(menuUuid: UUID)(implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable.filter(_.menuUuid === menuUuid)
    connection.db.run(query.result)
  }

  def getMenuPerDayByDate(date: Date)(implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    val query = menuPerDayTable.filter(_.date === date)
    connection.db.run(query.result)
  }

  def getAllMenuPerDays(implicit connection: DBConnection): Future[Seq[MenuPerDay]] = {
    connection.db.run(menuPerDayTable.result)
  }

  def removeMenuPerDay(uuid: UUID)(implicit connection: DBConnection): Future[Int]  = {
      val query = menuPerDayTable.filter(x => x.uuid === uuid).delete
      connection.db.run(query)
  }

  def removeMenuPerDayByMenuUuid(menuUuid: UUID)(implicit connection: DBConnection): Future[Int]  = {
    val query = menuPerDayTable.filter(x => x.menuUuid === menuUuid).delete
    connection.db.run(query)
  }

}

