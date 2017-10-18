package lunatech.lunchplanner.controllers

import java.io.ByteArrayInputStream

import akka.stream.scaladsl.StreamConverters
import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.data.Month
import lunatech.lunchplanner.services._
import lunatech.lunchplanner.viewModels.ReportForm
import org.joda.time.DateTime
import play.api.http.HttpEntity
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, EssentialAction, ResponseHeader, Result}
import play.api.{Configuration, Environment}
import play.mvc.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * It's serve the request for the reports between two dates
  *
  */
class ReportController @Inject()(
                                  userService: UserService,
                                  reportService: ReportService,
                                  val environment: Environment,
                                  val messagesApi: MessagesApi,
                                  val configuration: Configuration,
                                  implicit val connection: DBConnection) extends Controller with Secured with I18nSupport {

  val month = "month"
  val ChunkSize = 1024

  def getReport: EssentialAction =
    IsAdminAsync { username =>

      implicit request => {
        val reportMonth = request.session.get(month).map(_.toInt).getOrElse(getPreferredMonth)

        for {
          currentUser <- userService.getByEmailAddress(username)
          totalAttendees <- reportService.getReport(reportMonth)
          totalNotAttending <- reportService.getReportForNotAttending(reportMonth)
        } yield
          Ok(views.html.admin.report(
            getCurrentUser(currentUser, isAdmin = true, username),
            ReportForm.reportForm,
            totalAttendees,
            totalNotAttending,
            reportMonth))

      }
    }


  def filterAttendees: EssentialAction = IsAdminAsync { _ =>
    implicit request => {

      ReportForm
        .reportForm
        .bindFromRequest
        .fold(
          _ => {
            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.ReportController.getReport()))
          },
          selectedMonth => {
            val session = request.session + (month -> selectedMonth)
            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.ReportController.getReport())
              .withSession(session))
          })
    }
  }

  def export: EssentialAction = IsAdminAsync { _ =>
    implicit request => {
      val reportMonth = request.session.get(month).map(_.toInt).getOrElse(getPreferredMonth)

      for {
        totalAttendees <- reportService.getReport(reportMonth)
        totalNotAttending <- reportService.getReportForNotAttending(reportMonth)
      } yield {
        val inputStream = new ByteArrayInputStream(reportService.exportToExcel(totalAttendees, totalNotAttending))
        val month = Month.values(reportMonth - 1).month
        val content = StreamConverters.fromInputStream(() => inputStream, ChunkSize)
        Result(
          header = ResponseHeader(Http.Status.OK, Map(Http.HeaderNames.CONTENT_DISPOSITION -> s"attachment; filename=${month} report.xls")),
          body = HttpEntity.Streamed(content, None, Some("application/vnd.ms-excel"))
        )
      }
    }
  }

  def getPreferredMonth: Int = DateTime.now.minusMonths(1).getMonthOfYear

}
