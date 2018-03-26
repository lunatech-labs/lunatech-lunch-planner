package lunatech.lunchplanner.slack

import java.util.TimeZone

import akka.actor.{Actor, ActorSystem, Props}
import javax.inject.Inject
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import lunatech.lunchplanner.services.{
  MenuPerDayPerPersonService,
  SlackService,
  UserService
}
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * The scheduler for the LunchBot. It will execute at 10 AM every Tuesday.
  */
class LunchBotScheduler @Inject()(
    userService: UserService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    slackService: SlackService,
    lifecycle: ApplicationLifecycle,
    client: WSClient,
    conf: Configuration) {

  val system = ActorSystem("LunchBotActorSystem")
  var scheduler = QuartzSchedulerExtension(system)

  val scheduleName = "EveryTuesday"
  val lunchBotActor = system.actorOf(
    Props.create(classOf[LunchBotActor],
                 client,
                 conf.get[String]("slack.bot.host"),
                 userService,
                 menuPerDayPerPersonService,
                 slackService))

  scheduler.createSchedule(scheduleName,
                           None,
                           conf.get[String]("slack.bot.cron"),
                           None,
                           TimeZone.getDefault)

  scheduler.schedule(scheduleName, lunchBotActor, StartBot)

  lifecycle.addStopHook { () =>
    Future.successful(system.terminate)
  }
}

private class LunchBotActor(
    ws: WSClient,
    hostName: String,
    userService: UserService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    slackService: SlackService)
    extends Actor {

  override def receive: Receive = {
    case StartBot => act
  }

  def act: Unit = {
    val allEmails = userService.getAllEmailAddresses
    val filtered = getEmailAddressesOfUsersWhoHaveNoDecision(allEmails)
    val userIds = getSlackUserIdsByUserEmails(filtered)
    val channelIds = openConversation(userIds)
    val response = postMessages(channelIds)
    response onComplete {
      case Success(r) =>
        Logger.info(r)
      case Failure(t) => throw t
    }
  }

  def getEmailAddressesOfUsersWhoHaveNoDecision(
      allEmails: Future[Seq[String]]): Future[Seq[String]] = {
    val emailsOfAttendees =
      menuPerDayPerPersonService.getAttendeesEmailAddressesForUpcomingLunch

    for {
      all <- allEmails
      toFilterOut <- emailsOfAttendees
    } yield all.filterNot(toFilterOut.contains(_))
  }

  def getSlackUserIdsByUserEmails(
      emails: Future[Seq[String]]): Future[Seq[String]] = {
    for {
      emailList <- emails
      userIds <- slackService.getAllSlackUsersByEmails(emailList)
    } yield userIds
  }

  def openConversation(
      slackUserIds: Future[Seq[String]]): Future[Seq[String]] = {
    for {
      userIds <- slackUserIds
      channelIds <- slackService.openConversation(userIds)
    } yield channelIds
  }

  def postMessages(channelIds: Future[Seq[String]]): Future[String] = {
    for {
      channels <- channelIds
      response <- slackService.postMessage(channels)
    } yield response
  }

}

private case object StartBot
