package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.{ Inject, Singleton }

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Dish
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.viewModels.DishForm
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class DishService @Inject() (implicit val connection: DBConnection){

  def addNewDish(dishForm: DishForm): Future[Dish] = {
    val newDish = Dish(
      UUID.randomUUID(),
      dishForm.name,
      dishForm.description,
      dishForm.isVegetarian,
      dishForm.hasSeaFood,
      dishForm.hasPork,
      dishForm.hasBeef,
      dishForm.hasChicken,
      dishForm.isGlutenFree,
      dishForm.hasLactose,
      dishForm.remarks)

    DishTable.addDish(newDish)
  }

  def getAllDishes: Future[Seq[Dish]] = DishTable.getAllDishes

  def getDishByUuid(uuid: UUID): Future[Option[Dish]] = DishTable.getDishByUUID(uuid)

  def insertOrUpdateDish(uuid: UUID, dishForm: DishForm): Future[Dish] = {
    getDishByUuid(uuid)
      .flatMap {
        case Some(dish) =>
            val updatedDish = dish.copy(
              name = dishForm.name,
              description = dishForm.description,
              isVegetarian = dishForm.isVegetarian,
              hasSeaFood = dishForm.hasSeaFood,
              hasPork = dishForm.hasPork,
              hasBeef = dishForm.hasBeef,
              hasChicken = dishForm.hasChicken,
              isGlutenFree = dishForm.isGlutenFree,
              hasLactose = dishForm.hasLactose,
              remarks = dishForm.remarks)
            DishTable.insertOrUpdate(updatedDish)
              .flatMap {
                case true => Future.successful(updatedDish)
                case false => addNewDish(dishForm)
              }
          case None =>
            addNewDish(dishForm)
        }
      }
}
