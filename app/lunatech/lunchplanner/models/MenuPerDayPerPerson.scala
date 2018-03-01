package lunatech.lunchplanner.models

import java.util.UUID

final case class MenuPerDayPerPerson(
  uuid: UUID = UUID.randomUUID(),
  menuPerDayUuid: UUID,
  userUuid: UUID,
  isAttending: Boolean
)

final case class MenuWithNamePerDayPerPerson(
  menuPerDayUuid: UUID,
  menuDate: String,
  menuName: String,
  userUuid: UUID,
  isAttending: Option[Boolean],
  location: String
)

final case class MenuWithNameWithDishesPerPerson(
  menuPerDayUuid: UUID,
  menuDate: String,
  menuName: String,
  listOfDishes: Seq[Dish],
  userUuid: UUID,
  isAttending: Option[Boolean],
  location: String
)
