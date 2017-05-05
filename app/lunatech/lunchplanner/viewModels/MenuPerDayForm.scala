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
      "date" -> date(pattern = "dd-MM-yyyy")
    )(MenuPerDayForm.apply)(MenuPerDayForm.unapply)
  )
}

case class ListMenusPerDayForm(listUuids: List[UUID], dateStart: Date, dateEnd: Date)

object ListMenusPerDayForm {
  val listMenusPerDayForm = Form(
    mapping(
      "uuid" -> list(of[UUID]),
      "dateStart" -> date(pattern = "dd-MM-yyyy"),
      "dateEnd" -> date(pattern = "dd-MM-yyyy")
    )(ListMenusPerDayForm.apply)(ListMenusPerDayForm.unapply)
  )
}

case class FilterMenusPerDayForm(dateStart: Date, dateEnd: Date)

object FilterMenusPerDayForm {
  val filterMenusPerDayForm = Form(
    mapping(
      "dateStart" -> date(pattern = "dd-MM-yyyy"),
      "dateEnd" -> date(pattern = "dd-MM-yyyy")
    )(FilterMenusPerDayForm.apply)(FilterMenusPerDayForm.unapply)
  )
}
