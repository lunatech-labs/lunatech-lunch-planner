package lunatech.lunchplanner.models

import java.util.UUID

case class Menu(
  uuid: UUID = UUID.randomUUID(),
  name: String
)
