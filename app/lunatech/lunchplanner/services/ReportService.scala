package lunatech.lunchplanner.services

import com.google.inject.Inject
import java.sql.Date

import info.folone.scala.poi.{ Row, Sheet, StringCell, Workbook }

import scala.concurrent.ExecutionContext.Implicits.global
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDay, MenuPerDayAttendant, MenuWithNamePerDay, Report }
import org.joda.time.DateTime

import scala.concurrent.Future
import scalaz._

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
    attendees.map(menuAttendant => Report(menuAttendant.groupBy(_.date.toString).mapValues(_.map(_.name)), menuAttendant.size))
  }

  def exportToExcel(month:Int) = {

    val workbook = for {
      reportData <- getReport(month)
      usersCells <- Future.successful(reportData.usersPerDate.zipWithIndex.map { case ((date, users), index) =>
        val zipUsers = users.zipWithIndex
        val usersCells = zipUsers.map(user => StringCell(user._2, user._1 + 1))
        (date, usersCells)
        Set(Row(index) {
          Set(StringCell(1, date))
        },
          Row(index + 1) {
            usersCells.toSet
          })
      })
    } yield Workbook { Set(Sheet("Report") {
      usersCells.toSet.flatten })
    }

    workbook.map(_.safeToFile("file.xls").fold(ex ⇒ throw ex, identity).unsafePerformIO)
  }

//    val sheetTwo = Workbook {
//      Set(Sheet("name") {
//        Set(Row(1) {
//          Set(StringCell(1, "newdata"), StringCell(2, "data2"), StringCell(3, "data3"))
//        },
//          Row(2) {
//            Set(StringCell(1, "data"), StringCell(2, "data2"))
//          },
//          Row(3) {
//            Set(StringCell(1, "data"), StringCell(2, "data2"))
//          })
//      },
//        Sheet("name") {
//          Set(Row(2) {
//            Set(StringCell(1, "data"), StringCell(2, "data2"))
//          })
//        })
//    }
}
