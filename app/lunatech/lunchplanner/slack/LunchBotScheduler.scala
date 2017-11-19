package lunatech.lunchplanner.slack

import java.util.TimeZone

import akka.actor.{Actor, ActorSystem, Props}
import com.google.inject.Inject
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.http.{ContentTypes, HeaderNames}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * The scheduler for the LunchBot. It will execute every 10 AM on Tuesday and Thursday.
  */
class LunchBotScheduler @Inject()(val lifecycle: ApplicationLifecycle,
                                  val client: WSClient,
                                  val conf: Configuration){
  val system = ActorSystem("LunchBotActorSystem")
  var scheduler = QuartzSchedulerExtension(system)

  val scheduleName = "EveryTuesday"
  val lunchBotActor = system.actorOf(Props.create(classOf[LunchBotActor], client, conf.getString("slack.bot.host").get))
  scheduler.createSchedule(scheduleName,
    None,
    conf.getString("slack.bot.cron").get,
    None,
    TimeZone.getDefault)

  scheduler.schedule(scheduleName, lunchBotActor, "")

  lifecycle.addStopHook { () =>
    Future.successful(system.terminate)
  }
}

class LunchBotActor(val ws: WSClient, val hostName: String) extends Actor {
  override def receive: Receive = {
    case _: String => act
  }

  def act: Unit = {
    val allEmails = getAllEmailAddresses
    val filtered = getEmailAddressesOfUsersWhoHaveNoDecision(allEmails)
    val userIds = getSlackUserIdsByTheirEmails(filtered)
    val channelIds = openConversation(userIds)
    val response = postMessages(channelIds)
    response onComplete  {
      case Success(r) =>
        Logger.info(r)
      case Failure(t) => throw t
    }
  }

  /**
    * Email addresses from users of the Lunch App
    */
  def getAllEmailAddresses: Future[Seq[String]] = {
    val response = ws.url(s"$hostName/user/all/emailAddress").get
    response.map(r => r.json.as[Seq[String]])
  }

  def getEmailAddressesOfUsersWhoHaveNoDecision(allEmails: Future[Seq[String]]): Future[Seq[String]] = {
    val response = ws.url(s"$hostName/menuPerDayPerPerson/attendees").get
    val emailsOfAttendees = response.map(r => r.json.as[Seq[String]])

    for {
      all <- allEmails
      toFilterOut <- emailsOfAttendees
    } yield all.filterNot(toFilterOut.contains(_))
  }

  def getSlackUserIdsByTheirEmails(emails: Future[Seq[String]]): Future[Seq[String]] = {
    for {
      emailList <- emails
      response <- ws.url(s"$hostName/slack/users")
        .withHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
        .post(Json.toJson(emailList))
    } yield response.json.as[Seq[String]]
  }

  def openConversation(slackUserIds: Future[Seq[String]]) : Future[Seq[String]] = {
    for {
      userIds <- slackUserIds
      response <- ws.url(s"$hostName/slack/openConversation")
        .withHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
        .post(Json.toJson(userIds))
    } yield response.json.as[Seq[String]]
  }

  def postMessages(channelIds: Future[Seq[String]]) : Future[String] = {
    for {
      channels <- channelIds
      response <- ws.url(s"$hostName/slack/postMessages")
        .withHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
        .post(Json.toJson(channels))
    } yield response.json.as[String]
  }

}


