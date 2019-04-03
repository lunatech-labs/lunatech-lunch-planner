package lunatech.lunchplanner.schedulers

import java.util.TimeZone

import akka.actor.{ActorSystem, Props}
import javax.inject.Inject
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import lunatech.lunchplanner.schedulers.actors.{LunchBotActor, StartBot}
import lunatech.lunchplanner.services.{
  MenuPerDayPerPersonService,
  SlackService,
  UserService
}
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import play.api.Configuration

import scala.concurrent.Future

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
