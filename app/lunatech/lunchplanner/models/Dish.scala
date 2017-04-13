package lunatech.lunchplanner.models

import java.util.UUID

import play.api.libs.json.{ Json, OFormat }

case class Dish (
  uuid: UUID = UUID.randomUUID(),
  name: String,
  description: String,
  isVegetarian: Boolean = false,
  hasSeaFood: Boolean = false,
  hasPork: Boolean = false,
  hasBeef: Boolean = false,
  hasChicken: Boolean = false,
  isGlutenFree: Boolean = false,
  hasLactose: Boolean = false,
  remarks: Option[String] = None
)

case class DishIsSelected (
  uuid: UUID = UUID.randomUUID(),
  name: String,
  isSelected: Boolean
)

object Dish {
  implicit val dishFormat: OFormat[Dish] = Json.format[Dish]
}
