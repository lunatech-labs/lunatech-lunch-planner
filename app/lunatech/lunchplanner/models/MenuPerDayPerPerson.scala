package lunatech.lunchplanner.models

import java.util.UUID

case class MenuPerDayPerPerson(
  uuid: UUID = UUID.randomUUID(),
  menuPerDayUuid: UUID,
  userUuid: UUID
)

case class MenuWithNamePerDayPerPerson(
  menuPerDayUuid: UUID,
  menuDateAndName: String,
  userUuid: UUID,
  isSelected: Boolean
)
