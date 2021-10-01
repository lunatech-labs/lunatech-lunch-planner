package lunatech.lunchplanner.viewModels

import lunatech.lunchplanner.data.Location
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.{ Constraint, Invalid, Valid, ValidationError }
import play.api.libs.json.{ Json, OFormat }

import java.util.{ Date, UUID }

case class MenuPerDayForm(
    menuUuid: UUID,
    date: Date,
    location: String
)

object MenuPerDayForm {
  implicit val menuPerDayFormFormat: OFormat[MenuPerDayForm] =
    Json.format[MenuPerDayForm]

  val menuPerDayForm = Form(
    mapping(
      "menuUuid" -> of[UUID],
      "date" -> date(pattern = "dd-MM-yyyy"),
      "location" -> nonEmptyText.verifying(officeLocationConstraint)
    )(MenuPerDayForm.apply)(MenuPerDayForm.unapply)
  )

  def officeLocationConstraint: Constraint[String] =
    Constraint[String]("constraint.officelocation")({ text =>
      Location.forName(text) match {
        case Some(_) => Valid
        case None =>
          Invalid(ValidationError(s"$text is not a valid office location"))
      }
    })
}

case class ListMenusPerDayForm(listUuids: List[UUID],
                               dateStart: Date,
                               dateEnd: Date)

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
