package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Menu
import slick.driver.PostgresDriver.api._
import slick.lifted.{ ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuTable(tag: Tag) extends Table[Menu](tag, "Menu") {
  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def name: Rep[String] = column[String]("name")

  def * : ProvenShape[Menu] = (uuid, name) <> ((Menu.apply _).tupled, Menu.unapply)
}

object MenuTable {
  val menuTable: TableQuery[MenuTable] = TableQuery[MenuTable]

  def addMenu(menu: Menu)(implicit connection: DBConnection): Future[Menu] = {
    val query = menuTable returning menuTable += menu
    connection.db.run(query)
  }

  def menuExists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(menuTable.filter(_.uuid === uuid).exists.result)
  }

  def getMenuByUUID(uuid: UUID)(implicit connection: DBConnection): Future[Option[Menu]] = {
    menuExists(uuid).flatMap {
      case true =>
        val query = menuTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getMenuByName(name: String)(implicit connection: DBConnection): Future[Option[Menu]] = {
    val query = menuTable.filter(_.name === name)
    connection.db.run(query.result.headOption)
  }

  def getAllMenus(implicit connection: DBConnection): Future[Seq[Menu]] = {
    connection.db.run(menuTable.result)
  }

  def removeMenu(uuid: UUID)(implicit connection: DBConnection): Future[Int]  = {
    menuExists(uuid).flatMap {
      case true =>
        val query = menuTable.filter(x => x.uuid === uuid).delete
        connection.db.run(query)
      case false => Future(0)
    }
  }

}
