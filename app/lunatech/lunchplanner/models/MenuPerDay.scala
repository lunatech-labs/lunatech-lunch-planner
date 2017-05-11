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

case class MenuPerDayDietRestrictions(
  menuPerDayUuid: UUID,
  vegetarian: Int = 0,
  seaFoodRestriction: Int = 0,
  porkRestriction: Int = 0,
  beefRestriction: Int = 0,
  chickenRestriction: Int = 0,
  glutenRestriction: Int = 0,
  lactoseRestriction: Int = 0
)

case class MenuPerDayAttendant(name: String, otherRestrictions: String)
