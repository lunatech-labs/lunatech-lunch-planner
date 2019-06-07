package lunatech.lunchplanner.schedulers.actors

import akka.actor.Actor
import lunatech.lunchplanner.services.MonthlyReportService
import play.api.Logger

class MonthlyReportActor(service: MonthlyReportService) extends Actor {
  override def receive: Receive = {
    case SendLastMonthlyReport =>
      Logger.info("Triggering monthly report automatic")
      service.sendMonthlyReport()
  }
}

case object SendLastMonthlyReport
