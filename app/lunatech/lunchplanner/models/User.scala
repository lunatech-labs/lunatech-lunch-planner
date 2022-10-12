package lunatech.lunchplanner.models

import java.util.UUID

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Writes}

final case class User(
    uuid: UUID = UUID.randomUUID(),
    name: String,
    emailAddress: String,
    isAdmin: Boolean = false,
    isDeleted: Boolean = false
)

object User {
  implicit val writer: Writes[User] =
    (JsPath \ "uuid")
      .write[UUID]
      .and((JsPath \ "name").write[String])
      .and((JsPath \ "emailAddress").write[String])
      .and((JsPath \ "isAdmin").write[Boolean])
      .and((JsPath \ "isDeleted").write[Boolean])(unlift(User.unapply))
}
