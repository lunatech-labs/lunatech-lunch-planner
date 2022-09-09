package lunatech.lunchplanner.schedulers.actors

import akka.actor.{Actor, ActorLogging}
import lunatech.lunchplanner.services.MonthlyReportService

import scala.util.{Failure, Success}

class MonthlyReportActor(service: MonthlyReportService)
    extends Actor
    with ActorLogging {
  override def receive: Receive = { case SendLastMonthlyReport =>
    implicit val ec: scala.concurrent.ExecutionContext =
      scala.concurrent.ExecutionContext.global
    log.info("Triggering monthly report automatically.")

    service.sendMonthlyReport().map {
      case Right(_) =>
        log.info("Monthly report sent")
      case Left(exception) =>
        log.error(s"Error sending Monthly report: $exception")
    }
  }
}

case object SendLastMonthlyReport
