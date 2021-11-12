package lunatech.lunchplanner.schedulers.actors

import akka.actor.{ Actor, ActorLogging }
import lunatech.lunchplanner.services.MonthlyReportService

class MonthlyReportActor(service: MonthlyReportService)
    extends Actor
    with ActorLogging {
  override def receive: Receive = { case SendLastMonthlyReport =>
    log.info("Triggering monthly report automatically.")
    service.sendMonthlyReport()
  }
}

case object SendLastMonthlyReport
