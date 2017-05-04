package lunatech.lunchplanner.models

import java.sql.Date
import java.util.UUID

case class MenuPerDay(
  uuid: UUID = UUID.randomUUID(),
  menuUuid: UUID,
  date: Date
)

case class MenuWithNamePerDay(
  uuid: UUID,
  menuUuid: UUID,
  menuDate: String,
  menuName: String,
  numberOfPeopleSignedIn: Int
)
