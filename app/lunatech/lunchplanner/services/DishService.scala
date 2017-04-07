package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.{ Inject, Singleton }

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Dish
import lunatech.lunchplanner.persistence.DishTable
import lunatech.lunchplanner.viewModels.DishForm

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
}
