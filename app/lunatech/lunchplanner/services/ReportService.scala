package lunatech.lunchplanner.services

import com.google.inject.Inject
import java.sql.Date

import scala.concurrent.ExecutionContext.Implicits.global
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDay, Report }
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * Service to generate the reports like total number of attendees between two dates
  *
  * @param menuPerDayPerPersonService
  * @param menuPerDayService
  * @param connection
  */
class ReportService @Inject()(
                               menuPerDayPerPersonService: MenuPerDayPerPersonService,
                               menuPerDayService: MenuPerDayService,
                               implicit val connection: DBConnection) {

  def getReport(month: Int): Future[Report] = {
    val baseDate = DateTime.now.withMonthOfYear(month)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val attendees = menuPerDayService.getAllOrderedByDateFilterDateRange(new Date(sDate.getMillis), new Date(eDate.getMillis)).flatMap {
      Future.traverse(_) { (menuPerDay: MenuPerDay) =>
        menuPerDayPerPersonService.getListOfPeopleByMenuPerDayForReport(menuPerDay)
      }
    }.map(_.flatten)
    attendees.map(menuAttendant => Report(menuAttendant.groupBy(_.date.toString).mapValues(_.map(_.name))))
  }
}
