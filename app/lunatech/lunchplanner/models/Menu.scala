package lunatech.lunchplanner.models

import java.util.UUID

final case class Menu(
    uuid: UUID = UUID.randomUUID(),
    name: String
)
