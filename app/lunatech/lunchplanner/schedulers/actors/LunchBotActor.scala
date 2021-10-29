package lunatech.lunchplanner.schedulers.actors

import akka.actor.Actor
import lunatech.lunchplanner.services.{ MenuPerDayPerPersonService, SlackService, UserService }
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class LunchBotActor(userService: UserService,
                    menuPerDayPerPersonService: MenuPerDayPerPersonService,
                    slackService: SlackService)
    extends Actor with Logging {

  override def receive: Receive = {
    case RunBot => sendMessagesToUsers()
  }

  def sendMessagesToUsers(): Unit = {
    logger.info("Received RunBot message")

    for{
      allEmails <- userService.getAllEmailAddresses
      _ = logger.info("Got all users email addresses")
      emailsNoDecision <- getEmailAddressesOfUsersWhoHaveNoDecision(allEmails)
      _ = logger.info("Got all users that have not answered on lunch planner email addresses")
      slackUserIds <- getSlackUserIdsByUserEmails(emailsNoDecision)
      _ = logger.info("Got all users slack users ids")
      channelIds <- openConversation(slackUserIds)
      _ = logger.info("Got all users channels ids")
      response = postMessages(channelIds)
    } yield response onComplete {
      case Success(res) =>
        logger.info(res)
      case Failure(exception) =>
        logger.error(exception.getMessage, exception)
    }
  }

  def getEmailAddressesOfUsersWhoHaveNoDecision(allEmails: Seq[String]): Future[Seq[String]] =
    menuPerDayPerPersonService.getAttendeesEmailAddressesForUpcomingLunch.map(
      emailsOfAttendees => allEmails.filterNot(emailsOfAttendees.contains(_)))

  def getSlackUserIdsByUserEmails(emails: Seq[String]): Future[Seq[String]] =
    slackService.getAllSlackUsersByEmails(emails)

  def openConversation(slackUserIds: Seq[String]): Future[Seq[String]] = slackService.openConversation(slackUserIds)

  def postMessages(channelIds: Seq[String]): Future[String] = slackService.postMessage(channelIds)
}

case object RunBot
