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

case class Profile(name: Option[String], email: Option[String])
case class Member(id: String, profile: Profile)

case class ResponseAction(name: String, `type`: String, value: String)
case class SlackUser(id: String, name: String)
case class SlackResponse(user: SlackUser, action: Seq[ResponseAction])

object SlackForm {

  implicit val attachmentsActionsWrites = new Writes[AttachmentsActions] {
    override def writes(actions: AttachmentsActions): JsValue = Json.obj(
      "name"  -> actions.name,
      "text"  -> actions.text,
      "type"  -> actions.`type`,
      "style" -> actions.style,
      "value" -> actions.value
    )
  }

  implicit val attachmentsWrites = new Writes[Attachments] {
    override def writes(attachments: Attachments): JsValue = Json.obj(
      "text"        -> attachments.text,
      "callback_id" -> attachments.callback_id,
      "actions"     -> attachments.actions
    )
  }

  implicit val profileWrites = new Writes[Profile] {
    override def writes(profile: Profile): JsValue = Json.obj(
      "name"  -> profile.name,
      "email" -> profile.email
    )
  }

  implicit val memberWrites = new Writes[Member] {
    override def writes(member: Member): JsValue = Json.obj(
      "id"      -> member.id,
      "profile" -> member.profile
    )
  }

  implicit val profileReads: Reads[Profile] = (
    (JsPath \ "name")
      .readNullable[String]
      .and((JsPath \ "email").readNullable[String])
  )(Profile.apply _)

  implicit val memberReads: Reads[Member] = (
    (JsPath \ "id").read[String].and((JsPath \ "profile").read[Profile])
  )(Member.apply _)

  implicit val responseActionReads: Reads[ResponseAction] = (
    (JsPath \ "name")
      .read[String]
      .and((JsPath \ "type").read[String])
      .and((JsPath \ "value").read[String])
  )(ResponseAction.apply _)

  implicit val slackUserReads: Reads[SlackUser] = (
    (JsPath \ "id").read[String].and((JsPath \ "name").read[String])
  )(SlackUser.apply _)

  def jsonToString(attachments: Seq[Attachments]): String =
    Json.toJson(attachments).toString

  def jsonToSlackResponseObject(json: JsValue): SlackResponse =
    SlackResponse(
      (json \ "user").get.as[SlackUser],
      (json \ "actions").get.as[Seq[ResponseAction]]
    )

  def jsonToMemberObject(json: JsValue): Seq[Member] = {
    val jsonResult = (json \ "members").toOption
    if (jsonResult.isDefined) {
      jsonResult.get.as[Seq[Member]]
    } else {
      Seq()
    }

  }

}
