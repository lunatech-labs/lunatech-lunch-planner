package lunatech.lunchplanner.models

import java.util.UUID

import play.api.libs.json._

case class User (
  uuid: UUID,
  name: String,
  emailAddress: String,
  isAdmin: Boolean = false
)

object User {
  implicit val userFormat = Json.format[User]
}
