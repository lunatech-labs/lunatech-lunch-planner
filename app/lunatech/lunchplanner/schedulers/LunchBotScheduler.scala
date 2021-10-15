package lunatech.lunchplanner.schedulers

import akka.actor.{ ActorSystem, Props }
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import lunatech.lunchplanner.schedulers.actors.{ LunchBotActor, StartBot }
import lunatech.lunchplanner.services.{ MenuPerDayPerPersonService, SlackService, UserService }
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import java.util.TimeZone
import javax.inject.Inject
import scala.concurrent.Future

/**
  * The scheduler for the LunchBot
  */
class LunchBotScheduler @Inject()(
    userService: UserService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    slackService: SlackService,
    lifecycle: ApplicationLifecycle,
    conf: Configuration) {

  private val system = ActorSystem("LunchBotActorSystem")
  private val scheduler = QuartzSchedulerExtension(system)

  private val cronExpression = conf.get[String]("slack.bot.cron")
  private val scheduleName = "LunchBot"
  private val scheduleDescription = "Slack bot"

  val lunchBotActor = system.actorOf(
    Props.create(classOf[LunchBotActor],
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
    Future.successful(system.terminate())
  }
}
