package lunatech.lunchplanner.viewModels

import java.util.UUID

import play.api.libs.json.{ Json, OFormat }

case class MenuForm(
  menuName: String,
  dishesUuid: List[UUID]
)

object MenuForm {
  implicit val menuFormFormat: OFormat[MenuForm] = Json.format[MenuForm]
}
