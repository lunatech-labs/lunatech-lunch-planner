package lunatech.lunchplanner.viewModels

import java.sql.Date
import java.util.UUID

import play.api.libs.json.{ Json, OFormat }

case class MenuPerDayForm(
  menuUuid: UUID,
  date: String
)

object MenuPerDayForm {
  implicit val menuPerDayFormFormat: OFormat[MenuPerDayForm] = Json.format[MenuPerDayForm]
}
