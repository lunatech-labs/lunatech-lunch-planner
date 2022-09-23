package lunatech.lunchplanner.services

import lunatech.lunchplanner.models.{MenuPerDay, MenuPerDayPerPerson, User}
import lunatech.lunchplanner.viewModels._
import play.api.http.ContentTypes
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logging}
import play.mvc.Http.HeaderNames

import java.text.SimpleDateFormat
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SlackService @Inject() (
    val userService: UserService,
    val menuPerDayPerPersonService: MenuPerDayPerPersonService,
    val menuPerDayService: MenuPerDayService,
    val ws: WSClient,
    val configuration: Configuration
) extends Logging {

  val token: String = configuration.get[String]("slack.api.token")
  val sdf           = new SimpleDateFormat("dd-MM-yyyy")

  /** Will get the Lunatech e-mail address of the Slack user. We use the e-mail
    * as this is the data that is common between Slack and Lunch App.
    */
  def getAllSlackUsersByEmails(
      emails: Seq[String]
  ): Future[Either[String, Seq[String]]] = {
    val requestBody = Map("token" -> Seq(token))

    for {
      response <- doPost(getString("slack.api.usersList.url"), requestBody)
      statusResult <- Future.successful(
        SlackForm
          .jsonToResponseStatus(response.json) match {
          case Left(error) =>
            Left(s"Error getting slack user's list: $error")
          case Right(isOk) =>
            if (isOk) {
              SlackForm.jsonToMemberObject(response.json).map { members =>
                members
                  .filter(member => !member.deleted &&
                    member.profile.email.isDefined && emails.contains(
                      member.profile.email.get
                    )
                  )
                  .map(_.id)
              }
            } else {
              Left(s"Error getting slack user's list: ${SlackForm
                  .jsonToErrorMessage(response.json)}")
            }
        }
      )
    } yield statusResult
  }

  /** Will open a direct message channel to start a conversation. The channel ID
    * of this conversation is returned.
    */
  def openConversation(
      userIds: Seq[String]
  ): Future[Seq[Either[String, String]]] =
    Future.sequence(userIds.map { user =>
      val requestBody = Map("token" -> Seq(token), "users" -> Seq(user))
      for {
        response <- doPost(
          getString("slack.api.conversations.open"),
          requestBody
        )
        statusResult <- Future.successful(
          SlackForm
            .jsonToResponseStatus(response.json) match {
            case Left(error) =>
              Left(s"Error posting slack conversations open: $error")
            case Right(isOk) =>
              if (isOk) {
                SlackForm.jsonToChannelIdObject(response.json)
              } else { Left(SlackForm.jsonToErrorMessage(response.json)) }
          }
        )
      } yield statusResult
    })

  /** Will post a message to a direct message channel (channel ID starts with a
    * D) with attachments.
    */
  // scalastyle:off method.length
  def postMessage(channelIds: Seq[String]): Future[Unit] = {
    val text = getString("slack.bot.message.salutation")

    def sendMessage(
        attachment: Attachments
    ): String => Future[Unit] = { channelId =>
      val requestBody =
        Map(
          "token"       -> Seq(token),
          "channel"     -> Seq(channelId),
          "text"        -> Seq(text),
          "attachments" -> Seq(SlackForm.jsonToString(Seq(attachment)))
        )

      for {
        response <- doPost(
          getString("slack.api.postMessage.url"),
          requestBody
        )
        statusResult <- Future.successful(
          SlackForm
            .jsonToResponseStatus(response.json) match {
            case Left(error) =>
              logger.error(s"Error getting slack user's list: $error")
            case Right(isOk) =>
              if (!isOk) {
                logger.error(s"Error posting message to slack: ${SlackForm
                    .jsonToErrorMessage(response.json)}")
              }
          }
        )
      } yield ()
    }

    def sendMessages(attachments: Seq[Attachments]): Future[Unit] = {
      attachments.map { attachment =>
        Future.traverse(channelIds)(sendMessage(attachment))
      }

      if (attachments.isEmpty) {
        logger.info(
          "No message sent by SlackBot because there's no upcoming lunch the upcoming week."
        )
      } else {
        logger.info(s"SlackBot message sent to ${channelIds.length} people!")
      }

      Future.successful(())
    }

    for {
      attachments <- getAttachments
      _           <- sendMessages(attachments)
    } yield ()
  }
  // scalastyle:on method.length

  /** * Compute the response for the user on Slack
    */
  def processSlackResponse(response: SlackResponse): Future[String] = {
    val isAttending =
      response.action.forall(action => isUserAttending(action.value))

    if (isAttending) {
      menuPerDayService.getMenuForUpcomingSchedule.map {
        menusForUpcomingSchedule =>
          val (menu, _): (MenuPerDay, String) = {
            val menuUuid = getMenuUuid(response)
            menusForUpcomingSchedule.filter { case (menu, _) =>
              menu.uuid.toString == menuUuid
            }.head
          }

          configuration
            .get[String]("slack.bot.response.text")
            .format(menu.date.toLocalDate.getDayOfWeek, menu.location)
      }
    } else {
      Future.successful(
        configuration.get[String]("slack.bot.response.notAttending.text")
      )
    }
  }

  private def getMenuUuid(response: SlackResponse): String =
    response.action.headOption
      .map(_.value)
      .fold {
        logger.error(s"Empty slack response ${response}")
        ""
      }(identity)

  /** Will save to the DB the user response to the Slack Bot.
    */
  def processSlackRequest(
      response: SlackResponse
  ): Future[Seq[MenuPerDayPerPerson]] =
    for {
      email <- getEmailAddressBySlackUserId(response.user.id)
      user  <- userService.getByEmailAddress(email)
      added <- addResponseToDb(user, response)
    } yield added

  private def addResponseToDb(
      user: Option[User],
      slackResponse: SlackResponse
  ): Future[Seq[MenuPerDayPerPerson]] = {

    def addToDb(
        actions: Seq[ResponseAction],
        user: User
    ): Future[Seq[MenuPerDayPerPerson]] =
      Future
        .sequence(
          actions.map(action => addMenuPerDayPerPerson(action.value, user.uuid))
        )
        .map(_.flatten)

    def addMenuPerDayPerPerson(
        value: String,
        userUuid: UUID
    ): Future[Seq[MenuPerDayPerPerson]] = {
      val isAttending = isUserAttending(value)
      if (isAttending) {
        insertAttendingData(UUID.fromString(value), userUuid, isAttending)
          .map(Seq(_))
      } else {
        val menuUuids = getListMenuUuids(value)
        Future.traverse(menuUuids) { menuUuid =>
          insertAttendingData(UUID.fromString(menuUuid), userUuid, isAttending)
        }
      }
    }

    user
      .map(addToDb(slackResponse.action, _))
      .getOrElse(
        Future.failed(
          new RuntimeException(
            s"Unexpected error when processing slack response to user $user."
          )
        )
      )
  }

  private def insertAttendingData(
      menuUuid: UUID,
      userUuid: UUID,
      isAttending: Boolean
  ): Future[MenuPerDayPerPerson] = menuPerDayPerPersonService.addOrUpdate(
    MenuPerDayPerPerson(
      menuPerDayUuid = menuUuid,
      userUuid = userUuid,
      isAttending = isAttending
    )
  )

  // A "~" in value means that the user picked "Not Attending".
  // Sample value is menuUuid1~menuUuid2 which will be added separately to the DB
  private def isUserAttending(response: String): Boolean =
    !response.contains("~")

  private def getListMenuUuids(listMenus: String): Seq[String] =
    listMenus.split("~").toList

  private def getEmailAddressBySlackUserId(
      slackUserId: String
  ): Future[String] = {
    val requestBody = Map("token" -> Seq(token), "user" -> Seq(slackUserId))
    for {
      response <- doPost(getString("slack.api.usersInfo.url"), requestBody)
    } yield (response.json \ "user" \ "profile" \ "email").as[String]
  }

  private def doPost(url: String, requestBody: Map[String, Seq[String]]) =
    ws.url(url)
      .withHttpHeaders(
        HeaderNames.CONTENT_TYPE -> ContentTypes.FORM
      )
      .post(requestBody)

  private def getAttachments: Future[Seq[Attachments]] = {
    def toAttachmentsActions(
        menuWithMenuNameList: (MenuPerDay, String)
    ): Seq[AttachmentsActions] = {
      val (menuPerDay, _) = menuWithMenuNameList
      val yesAction = {
        val text =
          getString("slack.bot.button.yes.text").format(menuPerDay.location)
        val value = menuPerDay.uuid.toString
        AttachmentsActions(text = text, value = value)
      }

      // A "~" in value means that the user picked "Not Attending".
      val noAction = AttachmentsActions(
        text = getString("slack.bot.button.no.text"),
        style = "danger",
        value = s"${menuPerDay.uuid}~"
      )
      Seq(yesAction, noAction)
    }

    for {
      menuWithMenuNameList <- menuPerDayService.getMenuForUpcomingSchedule
    } yield menuWithMenuNameList.map { menuWithMenuName =>
      val actions                = toAttachmentsActions(menuWithMenuName)
      val (menuPerDay, menuName) = menuWithMenuName
      val message =
        getString("slack.bot.attachment.text")
          .format(
            menuPerDay.date.toLocalDate.getDayOfWeek,
            menuPerDay.location,
            menuName
          )
      Attachments(message, s"`${menuPerDay.date}$menuName", actions)
    }
  }

  private def getString(key: String): String =
    configuration.get[String](key)

}
