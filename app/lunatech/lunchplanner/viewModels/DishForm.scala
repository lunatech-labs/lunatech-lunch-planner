package lunatech.lunchplanner.viewModels

import play.api.data.Form
import play.api.data.Forms.{ mapping, optional, text, _ }
import play.api.libs.json.{ Json, OFormat }

case class DishForm (
  name: String,
  description: String,
  isVegetarian: Boolean = false,
  hasSeaFood: Boolean = false,
  hasPork: Boolean = false,
  hasBeef: Boolean = false,
  hasChicken: Boolean = false,
  isGlutenFree: Boolean = false,
  hasLactose: Boolean = false,
  remarks: Option[String] = None
)

object DishForm {
  implicit val dishFormFormat: OFormat[DishForm] = Json.format[DishForm]

  val dishForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "isVegetarian" -> boolean,
      "hasSeaFood" -> boolean,
      "hasPork" -> boolean,
      "hasBeef" -> boolean,
      "hasChicken" -> boolean,
      "isGlutenFree" -> boolean,
      "hasLactose" -> boolean,
      "remarks" -> optional(text)
    )(DishForm.apply)(DishForm.unapply)
  )
}
