package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Menu, Dish, MenuDish }
import slick.driver.PostgresDriver.api._
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
  val MenuDishTable: TableQuery[MenuDishTable] = TableQuery[MenuDishTable]

  def addMenuDish(menuDish: MenuDish)(implicit connection: DBConnection): Future[MenuDish] = {
    val query = MenuDishTable returning MenuDishTable += menuDish
    connection.db.run(query)
  }

  def menuDishExists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(MenuDishTable.filter(_.uuid === uuid).exists.result)
  }

  def getMenuDishByUUID(uuid: UUID)(implicit connection: DBConnection): Future[Option[MenuDish]] = {
    menuDishExists(uuid).flatMap {
      case true =>
        val query = MenuDishTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getMenuDishByMenuUuid(menuUuid: UUID)(implicit connection: DBConnection): Future[Seq[MenuDish]] = {
    val query = MenuDishTable.filter(_.menuUuid === menuUuid)
    connection.db.run(query.result)
  }

  def getAllMenuDishes(implicit connection: DBConnection): Future[Seq[MenuDish]] = {
    connection.db.run(MenuDishTable.result)
  }

  def removeMenuDish(uuid: UUID)(implicit connection: DBConnection): Future[Int]  = {
    menuDishExists(uuid).flatMap {
      case true =>
        val query = MenuDishTable.filter(x => x.uuid === uuid).delete
        connection.db.run(query)
      case false => Future(0)
    }
  }

}

