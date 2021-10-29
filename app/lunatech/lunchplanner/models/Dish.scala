package lunatech.lunchplanner.models

import play.api.libs.json.{Json, OFormat}

import java.util.UUID
import scala.collection.mutable.ListBuffer

final case class Dish(
    uuid: UUID = UUID.randomUUID(),
    name: String,
    description: String,
    isVegetarian: Boolean = false,
    isHalal: Boolean = false,
    hasSeaFood: Boolean = false,
    hasPork: Boolean = false,
    hasBeef: Boolean = false,
    hasChicken: Boolean = false,
    isGlutenFree: Boolean = false,
    hasLactose: Boolean = false,
    remarks: Option[String] = None,
    isDeleted: Boolean = false
)

final case class DishIsSelected(
    uuid: UUID = UUID.randomUUID(),
    name: String,
    isSelected: Boolean
)

object Dish {
  implicit val dishFormat: OFormat[Dish] = Json.format[Dish]

  def getDishExtraDetails(dish: Dish): Seq[String] = {
    val list = ListBuffer[String]()
    if (dish.isVegetarian) list += "Vegetarian"
    if (dish.isHalal) list += "Halal"
    if (dish.isGlutenFree) list += "Gluten Free"
    if (dish.hasChicken) list += "Chicken"
    if (dish.hasSeaFood) list += "Sea Food"
    if (dish.hasPork) list += "Pork"
    if (dish.hasBeef) list += "Beef"
    if (dish.hasLactose) list += "Lactose"

    list.toList
  }
}
