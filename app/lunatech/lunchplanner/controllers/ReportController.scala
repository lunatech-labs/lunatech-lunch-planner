package lunatech.lunchplanner.controllers

import akka.stream.scaladsl.StreamConverters
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.data.Month
import lunatech.lunchplanner.models.ReportDate
import lunatech.lunchplanner.services._
import lunatech.lunchplanner.viewModels.ReportForm
import play.api.data.Form
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{ Configuration, Environment }
import play.mvc.Http

import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Displays list of people attending and not attending at where on when
  */
class ReportController @Inject()(userService: UserService,
                                 reportService: ReportService,
                                 val controllerComponents: ControllerComponents,
                                 val environment: Environment,
                                 val configuration: Configuration)
    extends BaseController
    with Secured
    with I18nSupport {

  val month = "month"
  val year = "year"

  def getReport: EssentialAction = userAction.async { implicit request =>
    val reportMonth = getMonth(request.session)
    val reportYear = getYear(request.session)

    for {
      currentUser <- userService.getByEmailAddress(request.email)
      sortedReport <- reportService.getSortedReport(reportMonth, reportYear)
      totalNotAttending <- reportService.getReportForNotAttending(reportMonth,
                                                                  reportYear)
      isAdmin = currentUser.exists(user =>
        userService.isAdminUser(user.emailAddress))
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
      Future(
        Redirect(
          lunatech.lunchplanner.controllers.routes.ReportController.getReport))
    }

    def success: ReportDate => Future[Result] = {
      selectedReportDate: ReportDate =>
        val session = updateSession(request.session, selectedReportDate)
        Future {
          Redirect(
            lunatech.lunchplanner.controllers.routes.ReportController.getReport)
            .withSession(session)
        }
    }

    ReportForm.reportForm.bindFromRequest.fold(hasErrors, success)
  }

  def export: EssentialAction = adminAction.async { implicit request =>
    val chunkSize = 1024
    val reportMonth = getMonth(request.session)
    val reportYear = getYear(request.session)

    for {
      totalAttendees <- reportService.getReportByLocationAndDate(reportMonth,
                                                                 reportYear)
      totalNotAttending <- reportService.getReportForNotAttending(reportMonth,
                                                                  reportYear)
      inputStream = new ByteArrayInputStream(
        reportService.exportToExcel(totalAttendees, totalNotAttending))
      month = Month.values(reportMonth - 1).month
      content = StreamConverters.fromInputStream(() => inputStream, chunkSize)
    } yield
      Result(
        header = ResponseHeader(
          Http.Status.OK,
          Map(
            Http.HeaderNames.CONTENT_DISPOSITION -> s"""attachment; filename="$month $reportYear report.xls"""")),
        body =
          HttpEntity.Streamed(content, None, Some("application/vnd.ms-excel"))
      )
  }

  private def getDefaultDate: ReportDate = {
    val currentDate = LocalDateTime.now()
    ReportDate(month = currentDate.getMonthValue, year = currentDate.getYear)
  }

  private def getMonth(session: Session): Int =
    session.get(month).map(_.toInt).getOrElse(getDefaultDate.month)

  private def getYear(session: Session): Int =
    session.get(year).map(_.toInt).getOrElse(getDefaultDate.year)

  private def updateSession(session: Session, reportDate: ReportDate): Session =
    session + (month -> Integer.toString(reportDate.month)) + (year -> Integer
      .toString(reportDate.year))
}
