package lunatech.lunchplanner.viewModels

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.{ Json, OFormat }

import java.util.UUID

case class MenuForm(
    menuName: String,
    dishesUuid: List[UUID]
)

object MenuForm {
  implicit val menuFormFormat: OFormat[MenuForm] = Json.format[MenuForm]

  val menuForm = Form(
    mapping(
      "menuName" -> nonEmptyText,
      "dishesUuid" -> list(of[UUID])
    )(MenuForm.apply)(MenuForm.unapply)
  )
}

case class ListMenusForm(listUuids: List[UUID])

object ListMenusForm {
  val listMenusForm = Form(
    mapping(
      "uuid" -> list(of[UUID])
    )(ListMenusForm.apply)(ListMenusForm.unapply)
  )
}
