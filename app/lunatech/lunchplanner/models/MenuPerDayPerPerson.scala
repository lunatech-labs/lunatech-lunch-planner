package lunatech.lunchplanner.models

import java.util.UUID

case class MenuPerDayPerPerson(
  uuid: UUID = UUID.randomUUID(),
  menuPerDayUuid: UUID,
  userUuid: UUID
)

case class MenuWithNamePerDayPerPerson(
  menuPerDayUuid: UUID,
  menuDate: String,
  menuName: String,
  userUuid: UUID,
  isSelected: Boolean
)

case class MenuWithNameWithDishesPerPerson(
  menuPerDayUuid: UUID,
  menuDate: String,
  menuName: String,
  listOfDishes: Seq[Dish],
  userUuid: UUID,
  isSelected: Boolean
)
