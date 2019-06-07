package lunatech.lunchplanner.schedulers

import java.util.TimeZone

import akka.actor.{ActorSystem, Props}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import javax.inject.Inject
import lunatech.lunchplanner.schedulers.actors.{
  MonthlyReportActor,
  SendLastMonthlyReport
}
import lunatech.lunchplanner.services.MonthlyReportService
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

class MonthlyReportScheduler @Inject()(service: MonthlyReportService,
                                       conf: Configuration,
                                       lifecycle: ApplicationLifecycle) {
  private val system = ActorSystem("MonthlyReportActorSystem")
  private var scheduler = QuartzSchedulerExtension(system)
  private val scheduleName = "MonthlyReport"
  private val scheduleDescription =
    "Automatic generation and sending of monthly reports"
  private val cronExpression = conf.get[String]("monthly-report-scheduler.cron")
  private val monthlyReportActor =
    system.actorOf(Props.create(classOf[MonthlyReportActor], service))

  scheduler.createSchedule(scheduleName,
                           Some(scheduleDescription),
                           cronExpression,
                           None,
                           TimeZone.getDefault)

  scheduler.schedule(scheduleName, monthlyReportActor, SendLastMonthlyReport)

  lifecycle.addStopHook { () =>
    Future.successful(system.terminate)
  }
}
