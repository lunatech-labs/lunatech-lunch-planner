package lunatech.lunchplanner.models

import java.util.UUID

case class MenuDish(
  uuid: UUID = UUID.randomUUID(),
  menuUuid: UUID,
  dishUuid: UUID
)

case class MenuWithDishes(
  uuid: UUID,
  name: String,
  listOfDishes: Seq[Dish]
)

case class MenuWithAllDishesAndIsSelected(
  uuid: UUID,
  name: String,
  listOfDishes: Seq[DishIsSelected]
)
