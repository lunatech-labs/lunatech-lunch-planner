package lunatech.lunchplanner.viewModels

import java.util.UUID

import play.api.data.Form
import play.api.data.Forms.{ list, mapping, of }
import play.api.data.format.Formats._
import play.api.libs.json.{ Json, OFormat }

case class MenuPerDayPerPersonForm(
  menuPerDayUuid: List[UUID]
)

object MenuPerDayPerPersonForm {
  implicit val menuPerDayPerPersonFormFormat: OFormat[MenuPerDayPerPersonForm] = Json.format[MenuPerDayPerPersonForm]

  val menuPerDayPerPersonForm = Form(
    mapping(
      "menuPerDayUuid" -> list(of[UUID])
    )(MenuPerDayPerPersonForm.apply)(MenuPerDayPerPersonForm.unapply)
  )
}
