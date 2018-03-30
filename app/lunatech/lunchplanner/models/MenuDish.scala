package lunatech.lunchplanner.models

import java.util.UUID

final case class MenuDish(
    uuid: UUID = UUID.randomUUID(),
    menuUuid: UUID,
    dishUuid: UUID,
    isDeleted: Boolean = false
)

final case class MenuWithDishes(
    uuid: UUID,
    name: String,
    listOfDishes: Seq[Dish]
)

final case class MenuWithAllDishesAndIsSelected(
    uuid: UUID,
    name: String,
    listOfDishes: Seq[DishIsSelected]
)
