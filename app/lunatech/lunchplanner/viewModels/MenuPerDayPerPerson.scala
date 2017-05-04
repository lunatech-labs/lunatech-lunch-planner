package lunatech.lunchplanner.viewModels

import java.util.UUID

import play.api.libs.json.{ Json, OFormat }

case class MenuPerDayPerPersonForm(
  menuPerDayUuid: List[UUID]
)

object MenuPerDayPerPersonForm {
  implicit val menuPerDayPerPersonFormFormat: OFormat[MenuPerDayPerPersonForm] = Json.format[MenuPerDayPerPersonForm]
}
