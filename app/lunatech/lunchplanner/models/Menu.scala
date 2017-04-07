package lunatech.lunchplanner.models

import java.util.UUID

case class Menu(
  uuid: UUID = UUID.randomUUID(),
  name: String
)

case class MenuWithDishes(
  uuid: UUID,
  name: String,
  listOfDishes: Seq[Dish]
)
