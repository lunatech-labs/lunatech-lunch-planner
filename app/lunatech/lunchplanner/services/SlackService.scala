package lunatech.lunchplanner.services

import java.text.SimpleDateFormat
import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.models.MenuPerDayPerPerson
import lunatech.lunchplanner.viewModels.{ Attachments, AttachmentsActions, SlackForm }
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import play.mvc.Http.HeaderNames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SlackService @Inject()(val userService: UserService,
                             val menuPerDayPerPersonService: MenuPerDayPerPersonService,
                             val menuPerDayService: MenuPerDayService,
                             val ws: WSClient,
                             val configuration: Configuration) {

  val token = configuration.get[String]("slack.api.token")
  val sdf = new SimpleDateFormat("dd-MM-yyyy")

  /**
    * Will get the lunatech e-mail address of the Slack user.
    * We use the e-mail as this is the data that is common between Slack and Lunch App.
    */
  def getAllSlackUsersByEmails(emails: Seq[String]): Future[Seq[String]] = {
    val requestBody = Map("token" -> Seq(token))
    for {
      response <- doPost(getString("slack.api.usersList.url"), requestBody)
    } yield {
      val members = SlackForm.jsonToMemberObject(response.json)
      members.filter(member => member.profile.email.isDefined && emails.contains(member.profile.email.get)).map(_.id)
    }
  }

  /**
    * Will open a direct message channel to start a conversation.
    * The channel ID of this conversation is returned.
    */
  def openConversation(userIds: Seq[String]): Future[Seq[String]] = {
    val responses = userIds.map { userId =>
      val requestBody = Map("token" -> Seq(token),
                            "users" -> Seq(userId))
      val response = doPost(getString("slack.api.conversations.open"), requestBody)
      response.map(r => (r.json \ "channel" \ "id").as[String])
    }

    Future.sequence(responses)
  }

  /**
    * Will post a message to a direct message channel (channel ID starts with a D) with attachments.
    */
  def postMessage(channelIds: Seq[String]): Future[String] = {
    val text = getString("slack.bot.message.text")

    getAttachments.flatMap { attachments =>
      if(!attachments.isEmpty) {
        val responses = channelIds.map { channelId =>
          val requestBody = Map("token" -> Seq(token),
            "channel" -> Seq(channelId),
            "text" -> Seq(text),
            "attachments" -> Seq(SlackForm.jsonToString(attachments))
          )
          val response = doPost(getString("slack.api.postMessage.url"), requestBody)
          response.map(r => r.json)
        }
        Future.successful(s"Message sent to ${responses.length} people!")
      } else {
        Future.successful("No message sent because there's no upcoming lunch this coming Friday")
      }
    }
  }

  /**
    * Will handle the request from the Slack Bot.
    */
  def processSlackRequest(json: JsValue): Future[String] = {
    val slackResponse = SlackForm.jsonToSlackResponseObject(json)
    for {
      email <- getEmailAddressBySlackUserId(slackResponse.user.id)
      user <- userService.getByEmailAddress(email)
      menuForUpcomingSchedule <- menuPerDayService.getMenuForUpcomingSchedule
    } yield {
      val uuidAndMenuName = menuForUpcomingSchedule.map(a => a._1.uuid -> a._2)
      user.foreach { u =>
        slackResponse.action.foreach { action =>
          val value = action.value
          if (value.contains("~")) {
            value.split("~").foreach { uuid =>
              menuPerDayPerPersonService.add(
                MenuPerDayPerPerson(menuPerDayUuid = UUID.fromString(uuid),
                  userUuid = u.uuid,
                  isAttending = false)
              )
            }
          } else {
            menuPerDayPerPersonService.add(
              MenuPerDayPerPerson(menuPerDayUuid = UUID.fromString(value),
                                  userUuid = u.uuid,
                                  isAttending = true)
            )
          }
        }
      }

      val value = slackResponse.action.head.value
      if (value.contains("~")) {
        configuration.get[String]("slack.bot.response.notAttending.text")
      } else {
        val response = uuidAndMenuName.filter(_._1 == UUID.fromString(value)).head._2
        configuration.get[String]("slack.bot.response.text").format(response)
      }

    }
  }

  private def getEmailAddressBySlackUserId(slackUserId: String): Future[String] = {
    val requestBody = Map("token" -> Seq(token), "user" -> Seq(slackUserId))
    for {
      response <- doPost(getString("slack.api.usersInfo.url"), requestBody)
    } yield (response.json \ "user" \ "profile" \ "email").as[String]
  }

  private def doPost(url: String, requestBody: Map[String, Seq[String]]) = {
    ws.url(url)
      .withHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.FORM)
      .post(requestBody)
  }

  private def getAttachments: Future[Seq[Attachments]] = {
    val menuForUpcomingSchedule = menuPerDayService.getMenuForUpcomingSchedule
    menuForUpcomingSchedule.flatMap { menuWithMenuNameList =>
      if (!menuWithMenuNameList.isEmpty) {
        val actions = menuWithMenuNameList.map(menu =>
          AttachmentsActions(text = getString("slack.bot.button.yes.text").format(menu._2, menu._1.location),
            value = menu._1.uuid.toString)
        ) :+ AttachmentsActions(text = getString("slack.bot.button.no.text"),
                                style = "danger",
                                value = if (menuWithMenuNameList.length == 1) {
                                  s"${menuWithMenuNameList.head._1.uuid}~"
                                } else {
                                  menuWithMenuNameList.map(_._1.uuid).mkString("~")
                                })
        Future.successful(
          Seq(Attachments(getString("slack.bot.attachment.text").format(menuWithMenuNameList.map(s => s"${s._2} in ${s._1.location}").mkString(" and ")),
            "callback_id",
            actions))
        )
      } else {
        Future.successful(Seq())
      }
    }
  }

  private def getString(key: String): String = {
    configuration.get[String](key)
  }

}


