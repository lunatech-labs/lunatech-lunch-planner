package lunatech.lunchplanner.models

import java.util.UUID

case class MenuDish(
  uuid: UUID = UUID.randomUUID(),
  menuUuid: UUID,
  dishUuid: UUID
)
