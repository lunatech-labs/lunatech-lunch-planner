package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Dish
import slick.driver.PostgresDriver.api._
import slick.lifted.{ ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DishTable(tag: Tag) extends Table[Dish](tag, "Dish") {
  def uuid: Rep[UUID] = column[UUID]("uuid", O.PrimaryKey)

  def name: Rep[String] = column[String]("name")

  def description: Rep[String] = column[String]("description")

  def isVegetarian: Rep[Boolean] = column[Boolean]("isVegetarian")

  def hasSeaFood: Rep[Boolean] = column[Boolean]("hasSeaFood")

  def hasPork: Rep[Boolean] = column[Boolean]("hasPork")

  def hasBeef: Rep[Boolean] = column[Boolean]("HasBeef")

  def hasChicken: Rep[Boolean] = column[Boolean]("hasChicken")

  def isGlutenFree: Rep[Boolean] = column[Boolean]("isGlutenFree")

  def hasLactose: Rep[Boolean] = column[Boolean]("hasLactose")

  def remarks: Rep[String] = column[String]("remarks")

  def * : ProvenShape[Dish] =
    (uuid, name, description, isVegetarian, hasSeaFood, hasPork, hasBeef, hasChicken, isGlutenFree, hasLactose, remarks.?) <> ((Dish.apply _).tupled, Dish.unapply)
}

object DishTable {
  val dishTable: TableQuery[DishTable] = TableQuery[DishTable]

  def addDish(dish: Dish)(implicit connection: DBConnection): Future[Dish] = {
    val query = dishTable returning dishTable += dish
    connection.db.run(query)
  }

  def dishExists(uuid: UUID)(implicit connection: DBConnection): Future[Boolean] = {
    connection.db.run(dishTable.filter(_.uuid === uuid).exists.result)
  }

  def getDishByUUID(uuid: UUID)(implicit connection: DBConnection): Future[Option[Dish]] = {
    dishExists(uuid).flatMap {
      case true =>
        val query = dishTable.filter(x => x.uuid === uuid)
        connection.db.run(query.result.headOption)
      case false => Future(None)
    }
  }

  def getDishByName(name: String)(implicit connection: DBConnection): Future[Option[Dish]] = {
    val query = dishTable.filter(_.name === name)
    connection.db.run(query.result.headOption)
  }

  def getAllVegetarianDishes()(implicit connection: DBConnection): Future[Seq[Dish]] = {
    val query = dishTable.filter(_.isVegetarian)
    connection.db.run(query.result)
  }

  def getAllSeaFoodDishes()(implicit connection: DBConnection): Future[Seq[Dish]] = {
    val query = dishTable.filter(_.hasSeaFood)
    connection.db.run(query.result)
  }

  def getAllPorkDishes()(implicit connection: DBConnection): Future[Seq[Dish]] = {
    val query = dishTable.filter(_.hasPork)
    connection.db.run(query.result)
  }

  def getAllBeefDishes()(implicit connection: DBConnection): Future[Seq[Dish]] = {
    val query = dishTable.filter(_.hasBeef)
    connection.db.run(query.result)
  }

  def getAllChickenDishes()(implicit connection: DBConnection): Future[Seq[Dish]] = {
    val query = dishTable.filter(_.hasChicken)
    connection.db.run(query.result)
  }

  def getAllGlutenFreeDishes()(implicit connection: DBConnection): Future[Seq[Dish]] = {
    val query = dishTable.filter(_.isGlutenFree)
    connection.db.run(query.result)
  }

  def getAllLactoseDishes()(implicit connection: DBConnection): Future[Seq[Dish]] = {
    val query = dishTable.filter(_.hasLactose)
    connection.db.run(query.result)
  }

  def getAllDishes(implicit connection: DBConnection): Future[Seq[Dish]] = {
    connection.db.run(dishTable.result)
  }

  def removeDish(uuid: UUID)(implicit connection: DBConnection): Future[Int]  = {
    dishExists(uuid).flatMap {
      case true =>
        val query = dishTable.filter(x => x.uuid === uuid).delete
        connection.db.run(query)
      case false => Future(0)
    }
  }
}
