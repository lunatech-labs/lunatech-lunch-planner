package lunatech.lunchplanner.viewModels

import java.util.UUID

import play.api.data.Form
import play.api.data.Forms.{ mapping, text, _ }
import play.api.data.format.Formats._
import play.api.libs.json.{ Json, OFormat }

case class MenuPerDayPerPersonForm(
  menuPerDayUuid: List[UUID],
  menuDate: List[String]
)

object MenuPerDayPerPersonForm {
  implicit val menuPerDayPerPersonFormFormat: OFormat[MenuPerDayPerPersonForm] = Json.format[MenuPerDayPerPersonForm]

  val menuPerDayPerPersonForm = Form(
    mapping(
      "menuPerDayUuid" -> list(of[UUID]),
      "menuDate" -> list(text)
    )(MenuPerDayPerPersonForm.apply)(MenuPerDayPerPersonForm.unapply)
  )
}
