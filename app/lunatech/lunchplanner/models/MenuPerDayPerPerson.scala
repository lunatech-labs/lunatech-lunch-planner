package lunatech.lunchplanner.models

import java.util.UUID
import play.api.libs.json.Json
import play.api.libs.json.Writes

final case class MenuPerDayPerPerson(
    uuid: UUID = UUID.randomUUID(),
    menuPerDayUuid: UUID,
    userUuid: UUID,
    isAttending: Boolean
)

object MenuPerDayPerPerson {
  implicit val writer: Writes[MenuPerDayPerPerson] =
    Json.writes[MenuPerDayPerPerson]
}

final case class MenuWithNamePerDayPerPerson(
    menuPerDayUuid: UUID,
    menuDate: String,
    menuName: String,
    userUuid: UUID,
    isAttending: Option[Boolean],
    location: String
)

final case class MenuWithNameWithDishesPerPerson(
    menuPerDayUuid: UUID,
    menuDate: String,
    menuName: String,
    listOfDishes: Seq[Dish],
    userUuid: UUID,
    isAttending: Option[Boolean],
    location: String
)
