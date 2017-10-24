package lunatech.lunchplanner.models

import java.util.UUID

case class MenuPerDayPerPerson(
  uuid: UUID = UUID.randomUUID(),
  menuPerDayUuid: UUID,
  userUuid: UUID,
  isAttending: Boolean
)

case class MenuWithNamePerDayPerPerson(
  menuPerDayUuid: UUID,
  menuDate: String,
  menuName: String,
  userUuid: UUID,
  isAttending: Option[Boolean],
  location: String
)

case class MenuWithNameWithDishesPerPerson(
  menuPerDayUuid: UUID,
  menuDate: String,
  menuName: String,
  listOfDishes: Seq[Dish],
  userUuid: UUID,
  isAttending: Option[Boolean],
  location: String
)
