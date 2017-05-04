package lunatech.lunchplanner.viewModels

import java.util.UUID

import play.api.data.Form
import play.api.data.Forms.{ mapping, of, _ }
import play.api.data.format.Formats._
import play.api.libs.json.{ Json, OFormat }

case class MenuPerDayForm(
  menuUuid: UUID,
  date: String
)

object MenuPerDayForm {
  implicit val menuPerDayFormFormat: OFormat[MenuPerDayForm] = Json.format[MenuPerDayForm]

  val menuPerDayForm = Form(
    mapping(
      "menuUuid" -> of[UUID],
      "date" -> nonEmptyText
    )(MenuPerDayForm.apply)(MenuPerDayForm.unapply)
  )
}
