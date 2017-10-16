package lunatech.lunchplanner.services

import java.io.ByteArrayOutputStream
import java.sql.Date

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{MenuPerDay, Report}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
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

  def exportToExcel(report: Report): Array[Byte] = {
    val workbook = new XSSFWorkbook
    val out = new ByteArrayOutputStream
    try {
      report.usersPerDate.foreach(dateAndUsers => {
        val date = dateAndUsers._1
        val users = dateAndUsers._2
        val firstRow = Array("Date:", date, "", "Total:", s"${users.length}")
        val sheet = workbook.createSheet(date)

        val cellStyle = workbook.createCellStyle
        val font = workbook.createFont
        font.setBold(true)
        cellStyle.setFont(font)

        val row = sheet.createRow(0)
        firstRow.zipWithIndex.foreach { column =>
          val cell = row.createCell(column._2)
          cell.setCellValue(column._1.toString)

          if (column._1 == "Date:" || column._1 == "Total:") {
            cell.setCellStyle(cellStyle)
          }
        }

        val secondRow = sheet.createRow(2)
        val secondRowCell = secondRow.createCell(0)
        secondRowCell.setCellValue("Attendees:")
        secondRowCell.setCellStyle(cellStyle)

        val rowSkips = 3
        users.zipWithIndex.foreach { user =>
          val row = sheet.createRow(user._2 + rowSkips)
          val cell = row.createCell(0)
          cell.setCellValue(user._1.toString)
        }
      })
      workbook.write(out)
    } finally {
      out.close()
      workbook.close()
    }

    out.toByteArray
  }

}
