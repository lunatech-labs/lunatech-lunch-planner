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
  * The scheduler for the LunchBot
  */
class LunchBotScheduler @Inject()(
    userService: UserService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    slackService: SlackService,
    lifecycle: ApplicationLifecycle,
    client: WSClient,
    conf: Configuration) {

  private val system = ActorSystem("LunchBotActorSystem")
  private var scheduler = QuartzSchedulerExtension(system)

  private val slackHost = conf.get[String]("slack.bot.host")
  private val cronExpression = conf.get[String]("slack.bot.cron")
  private val scheduleName = "LunchBot"
  private val scheduleDescription = "Slack bot"

  val lunchBotActor = system.actorOf(
    Props.create(classOf[LunchBotActor],
                 client,
                 slackHost,
                 userService,
                 menuPerDayPerPersonService,
                 slackService))

  scheduler.createSchedule(scheduleName,
                           Some(scheduleDescription),
                           cronExpression,
                           None,
                           TimeZone.getDefault)

  scheduler.schedule(scheduleName, lunchBotActor, StartBot)

  lifecycle.addStopHook { () =>
    Future.successful(system.terminate)
  }
}
