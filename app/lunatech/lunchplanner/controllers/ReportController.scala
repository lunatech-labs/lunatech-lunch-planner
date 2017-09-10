package lunatech.lunchplanner.controllers

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.services._
import lunatech.lunchplanner.viewModels.ReportForm
import org.joda.time.DateTime
import play.api.{ Configuration, Environment }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller

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

  def getReport =
    IsAdminAsync { username =>

      implicit request => {
        val reportMonth = request.session.get(month).map(_.toInt).getOrElse(getPreferedMonth)

        for {
          currentUser <- userService.getByEmailAddress(username)
          totalAttendees <- reportService.getReport(reportMonth)
        } yield
          Ok(views.html.admin.report(
            getCurrentUser(currentUser, isAdmin = true, username),
            ReportForm.reportForm,
            totalAttendees,
            reportMonth))

      }
    }


  def filterAttendees = IsAdminAsync { _ =>
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
  def getPreferedMonth: Int = DateTime.now.minusMonths(1).getMonthOfYear

}
