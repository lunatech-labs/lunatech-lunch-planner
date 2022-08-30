package lunatech.lunchplanner.schedulers.actors

import akka.actor.{Actor, ActorLogging}
import lunatech.lunchplanner.services.{
  MenuPerDayPerPersonService,
  SlackService,
  UserService
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LunchBotActor(
    userService: UserService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    slackService: SlackService
) extends Actor
    with ActorLogging {

  override def receive: Receive = { case RunBot =>
    sendMessagesToUsers()
  }

  def sendMessagesToUsers(): Unit = {
    log.info("Received RunBot message")

    for {
      allEmails        <- userService.getAllEmailAddresses
      emailsNoDecision <- getEmailAddressesOfUsersWhoHaveNoDecision(allEmails)
      _ = log.info(
        s"Number of users that did not answer to lunch this week: ${emailsNoDecision.length}"
      )
      slackUserIds <- getSlackUserIdsByUserEmails(emailsNoDecision)
      channelIds   <- getSlackChannels(slackUserIds)
      _ = log.info(s"Got all users channels ids. Count: ${channelIds.length}")
      _ <- postMessages(channelIds)
    } yield ()

  }

  def getEmailAddressesOfUsersWhoHaveNoDecision(
      allEmails: Seq[String]
  ): Future[Seq[String]] =
    menuPerDayPerPersonService.getAttendeesEmailAddressesForUpcomingLunch.map(
      emailsOfAttendees => allEmails.filterNot(emailsOfAttendees.contains(_))
    )

  def getSlackUserIdsByUserEmails(
      emails: Seq[String]
  ): Future[Seq[String]] =
    slackService.getAllSlackUsersByEmails(emails).map {
      case Right(data) =>
        log.info(s"Got all users slack users ids. Count ${data.length}")
        data
      case Left(error) =>
        log.error(error)
        Seq.empty
    }

  def getSlackChannels(slackUserIds: Seq[String]): Future[Seq[String]] = {
    val openConversationResponse = slackService
      .openConversation(slackUserIds)

    openConversationResponse.foreach(_.collect { case Left(error) =>
      log.error(error)
    })
    openConversationResponse.map(_.collect { case Right(channel) => channel })
  }

  def postMessages(channelIds: Seq[String]): Future[Unit] =
    slackService.postMessage(channelIds)
}

case object RunBot
