package lunatech.lunchplanner.viewModels

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Attachments(
    text: String,
    callback_id: String,
    actions: Seq[AttachmentsActions]
)

case class AttachmentsActions(
    name: String = "decision",
    text: String = "",
    `type`: String = "button",
    style: String = "",
    value: String = ""
)

/** From Slack API at https://api.slack.com/methods/users.list
  */
case class Profile(real_name: Option[String], email: Option[String])
case class Member(id: String, deleted: Boolean, profile: Profile)

case class ResponseAction(name: String, `type`: String, value: String)
case class SlackUser(id: String, name: String)
case class SlackResponse(user: SlackUser, action: Seq[ResponseAction])

object SlackForm {

  implicit val attachmentsActionsWrites: Writes[AttachmentsActions] =
    new Writes[AttachmentsActions] {
      override def writes(actions: AttachmentsActions): JsValue = Json.obj(
        "name"  -> actions.name,
        "text"  -> actions.text,
        "type"  -> actions.`type`,
        "style" -> actions.style,
        "value" -> actions.value
      )
    }

  implicit val attachmentsWrites: Writes[Attachments] =
    new Writes[Attachments] {
      override def writes(attachments: Attachments): JsValue = Json.obj(
        "text"        -> attachments.text,
        "callback_id" -> attachments.callback_id,
        "actions"     -> attachments.actions
      )
    }

  implicit val profileWrites: Writes[Profile] = new Writes[Profile] {
    override def writes(profile: Profile): JsValue = Json.obj(
      "name"  -> profile.real_name,
      "email" -> profile.email
    )
  }

  implicit val memberWrites = new Writes[Member] {
    override def writes(member: Member): JsValue = Json.obj(
      "id"      -> member.id,
      "profile" -> member.profile
    )
  }

  implicit val profileReads: Reads[Profile] =
    (JsPath \ "name")
      .readNullable[String]
      .and((JsPath \ "email").readNullable[String])(Profile.apply _)

  implicit val memberReads: Reads[Member] =
    (JsPath \ "id")
      .read[String]
      .and(
        (JsPath \ "deleted")
          .read[Boolean]
      )
      .and((JsPath \ "profile").read[Profile])(Member.apply _)

  implicit val responseActionReads: Reads[ResponseAction] =
    (JsPath \ "name")
      .read[String]
      .and((JsPath \ "type").read[String])
      .and((JsPath \ "value").read[String])(ResponseAction.apply _)

  implicit val slackUserReads: Reads[SlackUser] =
    (JsPath \ "id")
      .read[String]
      .and((JsPath \ "name").read[String])(SlackUser.apply _)

  def jsonToString(attachments: Seq[Attachments]): String =
    Json.toJson(attachments).toString

  def jsonToSlackResponseObject(json: JsValue): SlackResponse =
    SlackResponse(
      (json \ "user").get.as[SlackUser],
      (json \ "actions").get.as[Seq[ResponseAction]]
    )

  def jsonToResponseStatus(json: JsValue): Either[String, Boolean] = {
    val jsonResult = (json \ "ok").toOption
    if (jsonResult.isDefined) {
      Right(jsonResult.get.as[Boolean])
    } else {
      Left("Slack response did not have expected field 'ok'.")
    }
  }

  /** checking the result manually instead of using JsLookup.toEither to
    * simplify the error
    */
  def jsonToErrorMessage(json: JsValue): String = {
    val jsonResult = (json \ "error").toOption
    if (jsonResult.isDefined) {
      jsonResult.get.as[String]
    } else {
      "Slack response did not have expected field 'error'."
    }
  }

  /** checking the result manually instead of using JsLookup.toEither to
    * simplify the error
    */
  def jsonToMemberObject(json: JsValue): Either[String, Seq[Member]] = {
    val jsonResult = (json \ "members").toOption
    if (jsonResult.isDefined) {
      Right(jsonResult.get.as[Seq[Member]])
    } else {
      Left("Slack response did not have expected field 'members'.")
    }
  }

  /** checking the result manually instead of using JsLookup.toEither to
    * simplify the error
    */
  def jsonToChannelIdObject(json: JsValue): Either[String, String] = {
    val jsonResult = (json \ "channel" \ "id").toOption
    if (jsonResult.isDefined) {
      Right(jsonResult.get.as[String])
    } else {
      Left("Slack response did not have expected field 'channel \\ id'.")
    }
  }

}
