package lunatech.lunchplanner.services

import java.text.SimpleDateFormat
import java.util.UUID

import javax.inject.Inject
import lunatech.lunchplanner.models.{MenuPerDay, MenuPerDayPerPerson, User}
import lunatech.lunchplanner.viewModels._
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import play.mvc.Http.HeaderNames
import scalaz.EitherT.eitherT
import scalaz.{-\/, EitherT, Monad, \/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SlackService @Inject()(
    val userService: UserService,
    val menuPerDayPerPersonService: MenuPerDayPerPersonService,
    val menuPerDayService: MenuPerDayService,
    val ws: WSClient,
    val configuration: Configuration) {

  val token: String = configuration.get[String]("slack.api.token")
  val sdf = new SimpleDateFormat("dd-MM-yyyy")

  implicit val futureMonad: Monad[Future] = new Monad[Future] {
    def point[A](a: => A): Future[A] = Future(a)
    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = fa flatMap f
  }

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
      members
        .filter(
          member =>
            member.profile.email.isDefined && emails.contains(
              member.profile.email.get))
        .map(_.id)
    }
  }

  /**
    * Will open a direct message channel to start a conversation.
    * The channel ID of this conversation is returned.
    */
  def openConversation(userIds: Seq[String]): Future[Seq[String]] = {
    Future.sequence {
      val responses = userIds.map { userId =>
        val requestBody = Map("token" -> Seq(token), "users" -> Seq(userId))
        val response =
          doPost(getString("slack.api.conversations.open"), requestBody)
        response.map(r => (r.json \ "channel" \ "id").as[String])
      }
      responses
    }
  }

  /**
    * Will post a message to a direct message channel (channel ID starts with a D) with attachments.
    */
  def postMessage(channelIds: Seq[String]): Future[String] = {
    val text = getString("slack.bot.message.text")

    def sendMessage(
        attachments: Seq[Attachments]): String => Future[JsValue] = {
      channelId =>
        val requestBody =
          Map("token" -> Seq(token),
              "channel" -> Seq(channelId),
              "text" -> Seq(text),
              "attachments" -> Seq(SlackForm.jsonToString(attachments)))
        val response =
          doPost(getString("slack.api.postMessage.url"), requestBody)
        response.map(r => r.json)
    }

    def result(attachments: Seq[Attachments]): Future[String] = {
      if (attachments.nonEmpty) {
        for {
          responses <- Future.traverse(channelIds)(sendMessage(attachments))
        } yield s"Message sent to ${responses.length} people!"
      } else {
        Future.successful(
          "No message sent because there's no upcoming lunch this coming Friday")
      }
    }

    for {
      attachments <- getAttachments
      response <- result(attachments)
    } yield response
  }

  /**
    * Will handle the request from the Slack Bot.
    */
  def processSlackRequest(json: JsValue): Future[String] = {
    val slackResponse = SlackForm.jsonToSlackResponseObject(json)

    for {
      email <- getEmailAddressBySlackUserId(slackResponse.user.id)
      user <- userService.getByEmailAddressT(email)
      menuForUpcomingSchedule <- menuPerDayService.getMenuForUpcomingSchedule
      result <- addResponseToDb(user, slackResponse).run
      value = slackResponse.action.head.value
    } yield {
      result match {
        case -\/(error) =>
          s"Error: $error. Please inform the admins about this."
        case \/-(isAttending) =>
          if (isAttending) {
            val uuidAndMenuName = menuForUpcomingSchedule.map {
              case (menuPerDay, menuName) => menuPerDay.uuid -> menuName
            }
            val response = uuidAndMenuName
              .filter { case (uuid, _) => uuid == UUID.fromString(value) }
              .head
              ._2
            configuration
              .get[String]("slack.bot.response.text")
              .format(response)
          } else {
            configuration.get[String]("slack.bot.response.notAttending.text")
          }
      }
    }
  }

  /**
    * A "~" in value means that the user picked "Not Attending". Sample value is menuUuid1~menuUuid2 which will be added separately to the DB
    */
  private def addResponseToDb(
      user: \/[String, User],
      slackResponse: SlackResponse): EitherT[Future, String, Boolean] = {
    def addToDb(actions: Seq[ResponseAction],
                user: User): Future[\/[String, Boolean]] = {
      val results = for {
        action <- actions
      } yield {
        val value = action.value
        for {
          menuUuids <- Future.successful(value.split("~").toList)
          _ <- addMenuPerDayPerPerson(value, user.uuid, menuUuids)
        } yield !value.contains("~")
      }

      Future.sequence(results).map {
        case r: Seq[Boolean] => \/-(r.forall(a => a))
        case _               => -\/(s"Error in addResponseToDb.")
      }
    }

    def addMenuPerDayPerPerson(value: String,
                               userUuid: UUID,
                               menuUuids: Seq[String]): Future[Equals] = {
      if (value.contains("~")) {
        Future.traverse(menuUuids) { menuUuid =>
          menuPerDayPerPersonService.add(
            MenuPerDayPerPerson(menuPerDayUuid = UUID.fromString(menuUuid),
                                userUuid = userUuid,
                                isAttending = false))
        }
      } else {
        menuPerDayPerPersonService.add(
          MenuPerDayPerPerson(menuPerDayUuid = UUID.fromString(value),
                              userUuid = userUuid,
                              isAttending = true))
      }
    }

    for {
      u <- eitherT(Future.successful(user))
      result <- eitherT(addToDb(slackResponse.action, u))
    } yield result
  }

  private def getEmailAddressBySlackUserId(
      slackUserId: String): Future[String] = {
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
    def toAttachmentsActions(menuWithMenuNameList: Seq[(MenuPerDay, String)])
      : Seq[AttachmentsActions] = {
      val yesActions = menuWithMenuNameList.map {
        case (menuPerDay, menuName) =>
          val text = getString("slack.bot.button.yes.text")
            .format(menuName, menuPerDay.location)
          val value = menuPerDay.uuid.toString
          AttachmentsActions(text = text, value = value)
      }
      val noAction = AttachmentsActions(
        text = getString("slack.bot.button.no.text"),
        style = "danger",
        value = if (menuWithMenuNameList.length == 1) {
          s"${menuWithMenuNameList.head._1.uuid}~"
        } else { menuWithMenuNameList.map(_._1.uuid).mkString("~") }
      )
      if (yesActions.nonEmpty) yesActions :+ noAction else Seq.empty
    }

    for {
      menuWithMenuNameList <- menuPerDayService.getMenuForUpcomingSchedule
    } yield {
      val actions = toAttachmentsActions(menuWithMenuNameList)
      val menuAndLocations = menuWithMenuNameList
        .map {
          case (menuPerDay, menuName) => s"$menuName in ${menuPerDay.location}"
        }
        .mkString(" and ")
      val message =
        getString("slack.bot.attachment.text").format(menuAndLocations)
      if (actions.nonEmpty) Seq(Attachments(message, "callback_id", actions))
      else Seq.empty
    }
  }

  private def getString(key: String): String = {
    configuration.get[String](key)
  }

}
