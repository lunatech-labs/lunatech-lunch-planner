package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Dish, Menu, MenuDish }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ ForeignKeyQuery, ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuDishTable(tag: Tag) extends Table[MenuDish](tag, "MenuDish") {
  private val menuTable = TableQuery[MenuTable]
  private val dishTable = TableQuery[DishTable]

  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def menuUuid: Rep[UUID] = column[UUID]("menuUuid")

  def dishUuid: Rep[UUID] = column[UUID]("dishUuid")

  def menuDishMenuForeignKey: ForeignKeyQuery[MenuTable, Menu] = foreignKey("menuDishMenu_fkey_", menuUuid, menuTable)(_.uuid)
  def menuDishDishForeignKey: ForeignKeyQuery[DishTable, Dish] = foreignKey("menuDishDish_fkey_", dishUuid, dishTable)(_.uuid)

  def * : ProvenShape[MenuDish] = (uuid, menuUuid, dishUuid) <> ((MenuDish.apply _).tupled, MenuDish.unapply)
}

object MenuDishTable {
  val menuDishTable: TableQuery[MenuDishTable] = TableQuery[MenuDishTable]

  def add(menuDish: MenuDish)(implicit connection: DBConnection): Future[MenuDish] = {
    val query = menuDishTable returning menuDishTable += menuDish
    connection.db.run(query)
  }

  def exists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(menuDishTable.filter(_.uuid === uuid).exists.result)
  }

  def getByUuid(uuid: UUID)(implicit connection: DBConnection): Future[Option[MenuDish]] = {
    exists(uuid).flatMap {
      case true =>
        val query = menuDishTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getByMenuUuid(menuUuid: UUID)(implicit connection: DBConnection): Future[Seq[MenuDish]] = {
    val query = menuDishTable.filter(_.menuUuid === menuUuid)
    connection.db.run(query.result)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[MenuDish]] = {
    connection.db.run(menuDishTable.result)
  }

  def remove(uuid: UUID)(implicit connection: DBConnection): Future[Int] = {
    val query = menuDishTable.filter(x => x.uuid === uuid).delete
    connection.db.run(query)
  }

  def removeByMenuUuid(menuUuid: UUID)(implicit connection: DBConnection) = {
    val query = menuDishTable.filter(x => x.menuUuid === menuUuid).delete
    connection.db.run(query)
  }

  def removeByDishUuid(dishUuid: UUID)(implicit connection: DBConnection) = {
    val query = menuDishTable.filter(x => x.dishUuid === dishUuid).delete
    connection.db.run(query)
  }
}

