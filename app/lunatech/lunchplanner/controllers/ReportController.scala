package lunatech.lunchplanner.controllers

import java.io.ByteArrayInputStream
import javax.inject.Inject

import akka.stream.scaladsl.StreamConverters
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.data.Month
import lunatech.lunchplanner.models.ReportDate
import lunatech.lunchplanner.services._
import lunatech.lunchplanner.viewModels.ReportForm
import org.joda.time.DateTime
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, BaseController, ControllerComponents, EssentialAction, ResponseHeader, Result }
import play.api.{ Configuration, Environment }
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
  val controllerComponents: ControllerComponents,
  val environment: Environment,
  val configuration: Configuration,
  implicit val connection: DBConnection) extends BaseController with Secured with I18nSupport {

  val month = "month"
  val year = "year"

  def getReport: EssentialAction =
    adminAction.async { implicit request =>
      val reportMonth = request.session.get(month).map(_.toInt).getOrElse(getDefaultDate.month)
      val reportYear = request.session.get(year).map(_.toInt).getOrElse(getDefaultDate.year)

      for {
        currentUser <- userService.getByEmailAddress(request.email)
        totalAttendees <- reportService.getReportByLocationAndDate(reportMonth, reportYear)
        totalNotAttending <- reportService.getReportForNotAttending(reportMonth, reportYear)
      } yield
        Ok(views.html.admin.report(
          getCurrentUser(currentUser, isAdmin = true, request.email),
          ReportForm.reportForm,
          totalAttendees,
          totalNotAttending,
          ReportDate(reportMonth, reportYear)
        ))
    }

  def filterAttendees: EssentialAction = adminAction.async { implicit request => {
      ReportForm
        .reportForm
        .bindFromRequest
        .fold(
          _ => {
            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.ReportController.getReport()))
          },
          selectedReportDate => {
            val session = request.session + (month -> Integer.toString(selectedReportDate.month)) + (year -> Integer.toString(selectedReportDate.year))
            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.ReportController.getReport())
              .withSession(session)
            )
          })
    }
  }

  def export: EssentialAction = adminAction.async { implicit request =>
    val ChunkSize = 1024
    val reportMonth = request.session.get(month).map(_.toInt).getOrElse(getDefaultDate.month)
    val reportYear = request.session.get(year).map(_.toInt).getOrElse(getDefaultDate.year)

    for {
      totalAttendees <- reportService.getReportByLocationAndDate(reportMonth, reportYear)
      totalNotAttending <- reportService.getReportForNotAttending(reportMonth, reportYear)
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

  def getDefaultDate: ReportDate = ReportDate(
    month = DateTime.now.getMonthOfYear,
    year = DateTime.now.getYear
  )
}
