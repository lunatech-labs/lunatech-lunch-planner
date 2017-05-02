package lunatech.lunchplanner.viewModels

import java.util.{ Date, UUID }

import play.api.data.Form
import play.api.data.Forms.{ mapping, of, _ }
import play.api.data.format.Formats._
import play.api.libs.json.{ Json, OFormat }

case class MenuPerDayForm(
  menuUuid: UUID,
  date: Date
)

object MenuPerDayForm {
  implicit val menuPerDayFormFormat: OFormat[MenuPerDayForm] = Json.format[MenuPerDayForm]

  val menuPerDayForm = Form(
    mapping(
      "menuUuid" -> of[UUID],
      "date" -> date(pattern = "dd-mm-yyyy")
    )(MenuPerDayForm.apply)(MenuPerDayForm.unapply)
//      verifying("Date cannot be earlier than today", f => f.date.after(new Date()))
  )
}

case class ListMenusPerDayForm(listUuids: List[UUID])

object ListMenusPerDayForm {
  val listMenusPerDayForm = Form(
    mapping(
      "uuid" -> list(of[UUID])
    )(ListMenusPerDayForm.apply)(ListMenusPerDayForm.unapply)
  )
}
