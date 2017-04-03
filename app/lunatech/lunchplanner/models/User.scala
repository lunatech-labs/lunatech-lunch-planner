package lunatech.lunchplanner.models

import java.util.UUID

import play.api.libs.json._

case class User (
  uuid: Option[UUID] = None,
  name: String,
  email: String,
  isAdmin: Boolean = false
)

object User {
  implicit val userFormat = Json.format[User]
}
