package lunatech.lunchplanner.schedulers.actors

import akka.actor.Actor
import lunatech.lunchplanner.services.MonthlyReportService
import play.api.Logging

class MonthlyReportActor(service: MonthlyReportService) extends Actor with Logging {
  override def receive: Receive = {
    case SendLastMonthlyReport =>
      logger.info("Triggering monthly report automatically.")
      service.sendMonthlyReport()
  }
}

case object SendLastMonthlyReport
