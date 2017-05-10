package lunatech.lunchplanner.viewModels

import play.api.data.Form
import play.api.data.Forms.{ mapping, optional, text, _ }
import play.api.libs.json.{ Json, OFormat }

case class ProfileForm(
  vegetarian: Boolean,
  seaFoodRestriction: Boolean,
  porkRestriction: Boolean,
  beefRestriction: Boolean,
  chickenRestriction: Boolean,
  glutenRestriction: Boolean,
  lactoseRestriction: Boolean,
  otherRestriction: Option[String]
)

object ProfileForm {
  implicit val profileFormFormat: OFormat[DishForm] = Json.format[DishForm]

  val profileForm = Form(
    mapping(
      "vegetarian" -> boolean,
      "seaFoodRestriction" -> boolean,
      "porkRestriction" -> boolean,
      "beefRestriction" -> boolean,
      "chickenRestriction" -> boolean,
      "glutenRestriction" -> boolean,
      "lactoseRestriction" -> boolean,
      "otherRestriction" -> optional(text)
    )(ProfileForm.apply)(ProfileForm.unapply)
  )
}
