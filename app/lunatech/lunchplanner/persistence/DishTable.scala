package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Dish
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DishTable(tag: Tag) extends Table[Dish](tag, _tableName = "Dish") {
  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def name: Rep[String] = column[String]("name")

  def description: Rep[String] = column[String]("description")

  def isVegetarian: Rep[Boolean] = column[Boolean]("isVegetarian")

  def hasSeaFood: Rep[Boolean] = column[Boolean]("hasSeaFood")

  def hasPork: Rep[Boolean] = column[Boolean]("hasPork")

  def hasBeef: Rep[Boolean] = column[Boolean]("hasBeef")

  def hasChicken: Rep[Boolean] = column[Boolean]("hasChicken")

  def isGlutenFree: Rep[Boolean] = column[Boolean]("isGlutenFree")

  def hasLactose: Rep[Boolean] = column[Boolean]("hasLactose")

  def remarks: Rep[String] = column[String]("remarks")

  def isDeleted: Rep[Boolean] = column[Boolean]("isDeleted")

  def * : ProvenShape[Dish] =
    (uuid,
     name,
     description,
     isVegetarian,
     hasSeaFood,
     hasPork,
     hasBeef,
     hasChicken,
     isGlutenFree,
     hasLactose,
     remarks.?,
     isDeleted) <> ((Dish.apply _).tupled, Dish.unapply)
}

object DishTable {
  val dishTable: TableQuery[DishTable] = TableQuery[DishTable]

  def add(dish: Dish)(implicit connection: DBConnection): Future[Dish] = {
    val query = dishTable returning dishTable += dish
    connection.db.run(query)
  }

  def getByUuid(uuid: UUID)(
      implicit connection: DBConnection): Future[Option[Dish]] = {
    val query = dishTable.filter(dish => dish.uuid === uuid)
    connection.db.run(query.result.headOption)
  }

  def getAll(implicit connection: DBConnection): Future[Seq[Dish]] = {
    connection.db.run(dishTable.filter(_.isDeleted === false).result)
  }

  def removeByUuid(uuid: UUID)(
      implicit connection: DBConnection): Future[Int] = {
    val query = dishTable.filter(_.uuid === uuid).map(_.isDeleted).update(true)
    connection.db.run(query)
  }

  def insertOrUpdate(dish: Dish)(
      implicit connection: DBConnection): Future[Boolean] = {
    val query = dishTable.insertOrUpdate(dish)
    connection.db.run(query).map(_ == 1)
  }
}
