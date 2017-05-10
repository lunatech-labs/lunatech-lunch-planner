package lunatech.lunchplanner.models

import java.util.UUID

case class UserProfile (
  userUuid: UUID,
  vegetarian: Boolean = false,
  seaFoodRestriction: Boolean = false,
  porkRestriction: Boolean = false,
  beefRestriction: Boolean = false,
  chickenRestriction: Boolean = false,
  glutenRestriction: Boolean = false,
  lactoseRestriction: Boolean = false,
  otherRestriction: Option[String] = None
)
