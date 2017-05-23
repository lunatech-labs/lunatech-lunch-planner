package lunatech.lunchplanner.controllers

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services._
import lunatech.lunchplanner.viewModels.{FilterMenusPerDayForm, FilterReportForm, ListMenusPerDayForm, ReportForm}
import play.api.{Configuration, Environment}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReportController @Inject()(
                                  userService: UserService,
                                  menuPerDayPerPersonService: MenuPerDayPerPersonService,
                                  reportService: ReportService,
                                  val environment: Environment,
                                  val messagesApi: MessagesApi,
                                  val configuration: Configuration,
                                  implicit val connection: DBConnection) extends Controller with Secured with I18nSupport {

  val DateStart = "dateStart"
  val DateEnd = "dateEnd"

  def getReport =
    IsAdminAsync { username =>
      implicit request => {
        val dStart = request.session.get(DateStart).map(java.sql.Date.valueOf).getOrElse(getDateStart)
        val dEnd = request.session.get(DateEnd).map(java.sql.Date.valueOf).getOrElse(getDateEnd)
        for {
          currentUser <- userService.getByEmailAddress(username)
          totalAttendees <- reportService.getReport(dStart, dEnd)
        } yield
          Ok(views.html.admin.report(
            getCurrentUser(currentUser, isAdmin = true, username),
            new SimpleDateFormat("dd-MM-yyyy").format(dStart),
            new SimpleDateFormat("dd-MM-yyyy").format(dEnd),
            ReportForm.reportForm,
            totalAttendees))
      }
    }


  def filterAttendees = IsAdminAsync { _ =>
    implicit request => {
      FilterReportForm
        .filterReportForm
        .bindFromRequest
        .fold(
          _ => {
            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.ReportController.getReport()))
          },
          filterDataForm => {
            val start = new SimpleDateFormat("yyyy-MM-dd").format(filterDataForm.dateStart)
            val end = new SimpleDateFormat("yyyy-MM-dd").format(filterDataForm.dateEnd)
            val session = request.session + (DateStart -> start) + (DateEnd -> end)

            Future.successful(
              Redirect(lunatech.lunchplanner.controllers.routes.ReportController.getReport())
                .withSession(session))
          })
    }
  }

  private def getDateStart = {
    val dateNow = LocalDate.now()
    java.sql.Date.valueOf(dateNow.withDayOfMonth(1))
  }

  private def getDateEnd = {
    val dateNow = LocalDate.now()
    val lastDate = dateNow.`with`(TemporalAdjusters.lastDayOfMonth())
    java.sql.Date.valueOf(lastDate)
  }

}
