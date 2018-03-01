package lunatech.lunchplanner.models

import java.util.UUID

final case class UserProfile (
  userUuid: UUID = UUID.randomUUID(),
  vegetarian: Boolean = false,
  seaFoodRestriction: Boolean = false,
  porkRestriction: Boolean = false,
  beefRestriction: Boolean = false,
  chickenRestriction: Boolean = false,
  glutenRestriction: Boolean = false,
  lactoseRestriction: Boolean = false,
  otherRestriction: Option[String] = None
)
