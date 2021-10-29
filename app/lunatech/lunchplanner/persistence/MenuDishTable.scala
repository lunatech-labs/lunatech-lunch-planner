package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{Dish, Menu, MenuDish}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuDishTable(tag: Tag)
    extends Table[MenuDish](tag, _tableName = "MenuDish") {
  private val menuTable = TableQuery[MenuTable]
  private val dishTable = TableQuery[DishTable]

  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def menuUuid: Rep[UUID] = column[UUID]("menuUuid")

  def dishUuid: Rep[UUID] = column[UUID]("dishUuid")

  def isDeleted: Rep[Boolean] = column[Boolean]("isDeleted")

  def menuDishMenuForeignKey: ForeignKeyQuery[MenuTable, Menu] =
    foreignKey(
      name = "menuDishMenu_fkey_",
      sourceColumns = menuUuid,
      targetTableQuery = menuTable
    )(_.uuid)
  def menuDishDishForeignKey: ForeignKeyQuery[DishTable, Dish] =
    foreignKey(
      name = "menuDishDish_fkey_",
      sourceColumns = dishUuid,
      targetTableQuery = dishTable
    )(_.uuid)

  def * : ProvenShape[MenuDish] =
    (
      uuid,
      menuUuid,
      dishUuid,
      isDeleted
    ) <> ((MenuDish.apply _).tupled, MenuDish.unapply)
}

object MenuDishTable {
  val menuDishTable: TableQuery[MenuDishTable] = TableQuery[MenuDishTable]

  def add(
      menuDish: MenuDish
  )(implicit connection: DBConnection): Future[MenuDish] = {
    val query = menuDishTable += menuDish
    connection.db.run(query).map(_ => menuDish)
  }

  def getByUuid(
      uuid: UUID
  )(implicit connection: DBConnection): Future[Option[MenuDish]] = {
    val query = menuDishTable.filter(_.uuid === uuid)
    connection.db.run(query.result.headOption)
  }

  def getByMenuUuid(
      menuUuid: UUID
  )(implicit connection: DBConnection): Future[Seq[MenuDish]] = {
    val query = menuDishTable.filter(menu =>
      menu.menuUuid === menuUuid && menu.isDeleted === false
    )
    connection.db.run(query.result)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[MenuDish]] =
    connection.db.run(menuDishTable.filter(_.isDeleted === false).result)

  def removeByUuid(
      uuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query =
      menuDishTable.filter(_.uuid === uuid).map(_.isDeleted).update(true)
    connection.db.run(query)
  }

  def removeByMenuUuid(
      menuUuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query = menuDishTable
      .filter(_.menuUuid === menuUuid)
      .map(_.isDeleted)
      .update(true)
    connection.db.run(query)
  }

  def removeByDishUuid(
      dishUuid: UUID
  )(implicit connection: DBConnection): Future[Int] = {
    val query = menuDishTable
      .filter(_.dishUuid === dishUuid)
      .map(_.isDeleted)
      .update(true)
    connection.db.run(query)
  }
}
