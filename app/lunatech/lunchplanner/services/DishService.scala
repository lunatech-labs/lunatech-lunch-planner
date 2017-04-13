package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Dish
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.viewModels.{ DishForm, ListDishesForm }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DishService @Inject() (implicit val connection: DBConnection){

  def add(dishForm: DishForm): Future[Dish] = {
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

    DishTable.add(newDish)
  }

  def getAll: Future[Seq[Dish]] = DishTable.getAll

  def getByUuid(uuid: UUID): Future[Option[Dish]] = DishTable.getByUUID(uuid)

  def insertOrUpdate(uuid: UUID, dishForm: DishForm): Future[Dish] = {
    getByUuid(uuid)
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
                case false => add(dishForm)
              }
          case None =>
            add(dishForm)
        }
      }

  def deleteMany(listDishes: ListDishesForm): Future[List[Int]] =
    Future.sequence(listDishes.listUuids.map(DishTable.remove))

  def delete(uuid: UUID): Future[Int] = DishTable.remove(uuid)
}
