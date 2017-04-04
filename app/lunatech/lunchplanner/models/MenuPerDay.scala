package lunatech.lunchplanner.models

import java.sql.Date
import java.util.UUID

case class MenuPerDay(
  uuid: UUID = UUID.randomUUID(),
  menuUuid: UUID,
  date: Date
)
