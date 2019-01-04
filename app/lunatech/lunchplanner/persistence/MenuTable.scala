package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Menu
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuTable(tag: Tag) extends Table[Menu](tag, _tableName = "Menu") {
  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def name: Rep[String] = column[String]("name")

  def isDeleted: Rep[Boolean] = column[Boolean]("isDeleted")

  def * : ProvenShape[Menu] =
    (uuid, name, isDeleted) <> ((Menu.apply _).tupled, Menu.unapply)
}

object MenuTable {
  val menuTable: TableQuery[MenuTable] = TableQuery[MenuTable]

  def add(menu: Menu)(implicit connection: DBConnection): Future[Menu] = {
    val query = menuTable += menu
    connection.db.run(query).map(_ => menu)
  }

  def getByUUID(uuid: UUID)(
      implicit connection: DBConnection): Future[Option[Menu]] = {
    val query = menuTable.filter(_.uuid === uuid)
    connection.db.run(query.result.headOption)
  }

  def getByName(name: String)(
      implicit connection: DBConnection): Future[Option[Menu]] = {
    val query = menuTable.filter(_.name === name)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[Menu]] = {
    connection.db.run(menuTable.filter(_.isDeleted === false).result)
  }

  def removeByUuid(uuid: UUID)(
      implicit connection: DBConnection): Future[Int] = {
    val query = menuTable.filter(_.uuid === uuid).map(_.isDeleted).update(true)
    connection.db.run(query)
  }

  def update(menu: Menu)(implicit connection: DBConnection): Future[Boolean] = {
    val query = menuTable
      .filter(_.uuid === menu.uuid)
      .map(m => (m.name, m.isDeleted))
      .update((menu.name, menu.isDeleted))
    connection.db.run(query).map(_ == 1)
  }
}
