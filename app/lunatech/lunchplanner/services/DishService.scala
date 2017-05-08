package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Dish
import lunatech.lunchplanner.persistence.DishTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DishService @Inject() (implicit val connection: DBConnection){

  def add(dish: Dish): Future[Dish] = {
    val newDish = Dish(
      UUID.randomUUID(),
      dish.name,
      dish.description,
      dish.isVegetarian,
      dish.hasSeaFood,
      dish.hasPork,
      dish.hasBeef,
      dish.hasChicken,
      dish.isGlutenFree,
      dish.hasLactose,
      dish.remarks)

    DishTable.add(newDish)
  }

  def getAll: Future[Seq[Dish]] = DishTable.getAll

  def getByUuid(uuid: UUID): Future[Option[Dish]] = DishTable.getByUUID(uuid)

  def insertOrUpdate(uuid: UUID, dishData: Dish): Future[Dish] = {
    getByUuid(uuid)
      .flatMap {
        case Some(dish) =>
            val updatedDish = dish.copy(
              name = dishData.name,
              description = dishData.description,
              isVegetarian = dishData.isVegetarian,
              hasSeaFood = dishData.hasSeaFood,
              hasPork = dishData.hasPork,
              hasBeef = dishData.hasBeef,
              hasChicken = dishData.hasChicken,
              isGlutenFree = dishData.isGlutenFree,
              hasLactose = dishData.hasLactose,
              remarks = dishData.remarks)
            DishTable.insertOrUpdate(updatedDish)
              .flatMap {
                case true => Future.successful(updatedDish)
                case false => add(dishData)
              }
          case None =>
            add(dishData)
        }
      }

  def delete(uuid: UUID): Future[Int] = DishTable.remove(uuid)
}
