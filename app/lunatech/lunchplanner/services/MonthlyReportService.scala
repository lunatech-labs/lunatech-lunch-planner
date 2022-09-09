package lunatech.lunchplanner.services

import lunatech.lunchplanner.configuration.EmailConfiguration
import play.api.i18n.MessagesApi
import play.api.{Configuration, Logging}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MonthlyReportService @Inject() (
    val configuration: Configuration,
    val messagesApi: MessagesApi,
    reportService: ReportService,
    emailService: EmailService,
    emailConfiguration: EmailConfiguration
) extends Logging {
  val oneMonth      = 1
  val fileExtension = "xls"

  def sendMonthlyReport(): Future[Either[Throwable, String]] = {
    val (month, year) = getPreviousMonthAndYear
    val monthName     = fromMonthNumberToName(month)

    val emailConf = emailConfiguration.getMonthlyReportEmailConfiguration()
    val emailConfWithMonth = emailConf.copy(
      subject = emailConf.subject + s" for the month of $monthName"
    )
    val messageBody =
      s"Please find the lunch planner monthly report attached, for the month of $monthName."

    val attachmentName = s"$monthName.$fileExtension"

    getLastAvailableReport(month, year).map { reportData =>
      logger.info("Monthly report data generated.")

      emailService.sendMessageWithAttachment(
        emailConfWithMonth,
        messageBody,
        attachmentName,
        reportData
      )
    }
  }

  private def getLastAvailableReport(
      month: Int,
      year: Int
  ): Future[Array[Byte]] =
    for {
      totalAttendees    <- reportService.getReportByLocationAndDate(month, year)
      totalNotAttending <- reportService.getReportForNotAttending(month, year)
    } yield reportService.exportToExcel(totalAttendees, totalNotAttending)

  private def getPreviousMonthAndYear: (Int, Int) = {
    val previousMonth = LocalDateTime.now().minusMonths(oneMonth)
    (previousMonth.getMonthValue, previousMonth.getYear)
  }

  // noinspection ScalaStyle
  private def fromMonthNumberToName(month: Int): String =
    month match {
      case 1  => "January"
      case 2  => "February"
      case 3  => "March"
      case 4  => "April"
      case 5  => "May"
      case 6  => "June"
      case 7  => "July"
      case 8  => "August"
      case 9  => "September"
      case 10 => "October"
      case 11 => "November"
      case 12 => "December"
    }
}
