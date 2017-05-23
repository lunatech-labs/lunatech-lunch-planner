package lunatech.lunchplanner.services

import com.google.inject.Inject
import java.sql.Date

import scala.concurrent.ExecutionContext.Implicits.global
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{MenuPerDay, MenuPerDayAttendant, MenuWithNamePerDay, Report}

import scala.concurrent.Future

class ReportService @Inject()(
                               menuPerDayPerPersonService: MenuPerDayPerPersonService,
                               menuPerDayService: MenuPerDayService,
                               implicit val connection: DBConnection) {

  def getReport(sDate: Date, eDate: Date) = {
    val attendees =menuPerDayService.getAllOrderedByDateFilterDateRange(sDate, eDate).flatMap {
      Future.traverse(_) { (menuPerDay: MenuPerDay) =>
        menuPerDayPerPersonService.getListOfPeopleByMenuPerDay(menuPerDay.uuid)
        }
    }.map(_.flatten)
    attendees.map(menuAttendant=>Report(menuAttendant.groupBy(_.name).mapValues(_.size),menuAttendant.size))
  }
}

