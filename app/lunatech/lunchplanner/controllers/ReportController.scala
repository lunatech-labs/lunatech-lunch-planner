package lunatech.lunchplanner.controllers

import java.io.ByteArrayInputStream

import akka.stream.scaladsl.StreamConverters
import javax.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.data.Month
import lunatech.lunchplanner.models.ReportDate
import lunatech.lunchplanner.services._
import lunatech.lunchplanner.viewModels.ReportForm
import org.joda.time.DateTime
import play.api.data.Form
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Environment}
import play.mvc.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Displays list of people attending and not attending at where on when
  */
class ReportController @Inject()(userService: UserService,
                                 reportService: ReportService,
                                 val controllerComponents: ControllerComponents,
                                 val environment: Environment,
                                 val configuration: Configuration,
                                 implicit val connection: DBConnection)
    extends BaseController
    with Secured
    with I18nSupport {

  val month = "month"
  val year = "year"

  def getReport: EssentialAction = userAction.async { implicit request =>
    val reportMonth =
      request.session.get(month).map(_.toInt).getOrElse(getDefaultDate.month)
    val reportYear =
      request.session.get(year).map(_.toInt).getOrElse(getDefaultDate.year)

    for {
      currentUser <- userService.getByEmailAddress(request.email)
      sortedReport <- reportService.getSortedReport(reportMonth, reportYear)
      totalNotAttending <- reportService.getReportForNotAttending(reportMonth,
                                                                  reportYear)
      isAdmin = if (currentUser.isDefined)
        userService.isAdminUser(currentUser.get.emailAddress)
      else false
    } yield
      Ok(
        views.html.admin.report(
          getCurrentUser(currentUser, isAdmin = isAdmin, request.email),
          ReportForm.reportForm,
          sortedReport,
          totalNotAttending,
          ReportDate(reportMonth, reportYear)
        )
      )
  }

  def filterAttendees: EssentialAction = userAction.async { implicit request =>
    def hasErrors: Form[ReportDate] => Future[Result] = { _ =>
      Future(Redirect(
        lunatech.lunchplanner.controllers.routes.ReportController.getReport()))
    }

    def success: ReportDate => Future[Result] = { selectedReportDate =>
      val session = request.session + (month -> Integer.toString(
        selectedReportDate.month)) +
        (year -> Integer.toString(selectedReportDate.year))
      Future {
        Redirect(
          lunatech.lunchplanner.controllers.routes.ReportController.getReport())
          .withSession(session)
      }
    }

    ReportForm.reportForm.bindFromRequest.fold(hasErrors, success)
  }

  def export: EssentialAction = adminAction.async { implicit request =>
    val ChunkSize = 1024
    val reportMonth =
      request.session.get(month).map(_.toInt).getOrElse(getDefaultDate.month)
    val reportYear =
      request.session.get(year).map(_.toInt).getOrElse(getDefaultDate.year)

    for {
      totalAttendees <- reportService.getReportByLocationAndDate(reportMonth,
                                                                 reportYear)
      totalNotAttending <- reportService.getReportForNotAttending(reportMonth,
                                                                  reportYear)
      inputStream = new ByteArrayInputStream(
        reportService.exportToExcel(totalAttendees, totalNotAttending))
      month = Month.values(reportMonth - 1).month
      content = StreamConverters.fromInputStream(() => inputStream, ChunkSize)
    } yield
      Result(
        header = ResponseHeader(
          Http.Status.OK,
          Map(
            Http.HeaderNames.CONTENT_DISPOSITION -> s"attachment; filename='$month $reportYear report.xls'")),
        body =
          HttpEntity.Streamed(content, None, Some("application/vnd.ms-excel"))
      )
  }

  private def getDefaultDate: ReportDate =
    ReportDate(month = DateTime.now.getMonthOfYear, year = DateTime.now.getYear)
}
