package lunatech.lunchplanner.services

import java.io.ByteArrayOutputStream
import java.sql.Date

import com.google.inject.Inject
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models._
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.{XSSFRow, XSSFSheet, XSSFWorkbook}
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

  def getReportByLocationAndDate(month: Int): Future[ReportByDateAndLocation] = {
    val baseDate = DateTime.now.withMonthOfYear(month)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val attendees: Future[Seq[MenuPerDayReportByDateAndLocation]] =
      menuPerDayService
        .getAllOrderedByDateFilterDateRange(new Date(sDate.getMillis),
                                            new Date(eDate.getMillis))
        .flatMap {
          Future.traverse(_) { (menuPerDay: MenuPerDay) =>
            menuPerDayPerPersonService
              .getListOfPeopleByMenuPerDayByLocationAndDateForReport(menuPerDay)
          }
        }
        .map(_.flatten)
    attendees.map(attendees => {
      ReportByDateAndLocation(attendees.groupBy(attendee => (attendee.date, attendee.location)).mapValues(_.map(_.attendeeName)))
    })
  }

  def getReportForNotAttending(month: Int): Future[Report] = {
    val baseDate = DateTime.now.withMonthOfYear(month)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val attendees = menuPerDayService.getAllAvailableDatesWithinRange(new Date(sDate.getMillis), new Date(eDate.getMillis)).flatMap {
      Future.traverse(_) { date: Date =>
        menuPerDayPerPersonService.getNotAttendingByDate(date)
      }
    }.map(_.flatten)
    attendees.map(menuAttendant => Report(menuAttendant.groupBy(_.date.toString).mapValues(_.map(_.name))))
  }

  def exportToExcel(report: Report, reportNotAttending: Report): Array[Byte] = {
    val workbook = new XSSFWorkbook
    val cellStyle = workbook.createCellStyle
    val font = workbook.createFont
    font.setBold(true)
    cellStyle.setFont(font)

    val out = new ByteArrayOutputStream
    try {
      report.usersPerDate.foreach(dateAndUsers => {
        val date = dateAndUsers._1
        val users = dateAndUsers._2
        val sheet = workbook.createSheet(date)

        initializeSheet(sheet, date, users, cellStyle)
        writeSecondRow(sheet.createRow(2), "Attendees:", cellStyle, 0)
        writeUserData(sheet, users, 0)
      })

      reportNotAttending.usersPerDate.foreach { dateAndUsers =>
        val date = dateAndUsers._1
        val users = dateAndUsers._2
        Option(workbook.getSheet(date)) match {
          case Some(s) =>
            writeSecondRow(s.getRow(2), "Did not attend:", cellStyle, 1)
            writeUserData(s, users, 1)
          case None =>
            val sheet = workbook.createSheet(date)
            initializeSheet(sheet, date, users, cellStyle)
            writeSecondRow(sheet.createRow(2), "Did not attend:", cellStyle, 0)
            writeUserData(sheet, users, 0)
        }
      }
      workbook.write(out)
    } finally {
      out.close()
      workbook.close()
    }

    out.toByteArray
  }

  private def initializeSheet(sheet: XSSFSheet, date: String, users: Seq[String], cellStyle: CellStyle) = {
    val row = sheet.createRow(0)
    val firstRow = Array("Date:", date, "", "Total:", s"${users.length}")
    firstRow.zipWithIndex.foreach { column =>
      val cell = row.createCell(column._2)
      cell.setCellValue(column._1.toString)

      if (column._1 == "Date:" || column._1 == "Total:") {
        cell.setCellStyle(cellStyle)
      }
    }
  }

  private def writeSecondRow(row: XSSFRow, cellValue: String, cellStyle: CellStyle, columnIndex: Int) = {
    val secondRowCell = row.createCell(columnIndex)
    secondRowCell.setCellValue(cellValue)
    secondRowCell.setCellStyle(cellStyle)
  }

  private def writeUserData(sheet: XSSFSheet, users: Seq[String], columnIndex: Int) = {
    val rowSkips = 3
    users.zipWithIndex.foreach { user =>
      val row = if(sheet.getRow(user._2 + rowSkips) == null) sheet.createRow(user._2 + rowSkips) else sheet.getRow(user._2 + rowSkips) //scalastyle:ignore
      val cell = row.createCell(columnIndex)
      cell.setCellValue(user._1.toString)
    }
  }

}
