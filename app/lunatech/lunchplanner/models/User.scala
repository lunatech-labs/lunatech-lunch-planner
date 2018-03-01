package lunatech.lunchplanner.models

import java.util.UUID

final case class User (
  uuid: UUID = UUID.randomUUID(),
  name: String,
  emailAddress: String,
  isAdmin: Boolean = false
)
