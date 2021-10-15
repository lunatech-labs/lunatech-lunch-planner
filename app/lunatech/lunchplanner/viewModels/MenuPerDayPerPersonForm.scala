package lunatech.lunchplanner.viewModels

import play.api.data.Form
import play.api.data.Forms.{ mapping, _ }
import play.api.data.format.Formats._
import play.api.libs.json.{ Json, OFormat }

import java.util.UUID

case class MenuPerDayPerPersonForm(
    menuPerDayUuids: List[UUID],
    menuPerDayUuidsNotAttending: List[String]
)

object MenuPerDayPerPersonForm {
  implicit val menuPerDayPerPersonFormFormat: OFormat[MenuPerDayPerPersonForm] =
    Json.format[MenuPerDayPerPersonForm]

  val menuPerDayPerPersonForm = Form(
    mapping(
      "menuPerDayUuids" -> list(of[UUID]),
      "menuPerDayUuidsNotAttending" -> list(text)
    )(MenuPerDayPerPersonForm.apply)(MenuPerDayPerPersonForm.unapply)
  )
}
