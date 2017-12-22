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

  implicit def ordered: Ordering[Date] = new Ordering[Date] {
    override def compare(x: Date, y: Date): Int = x compareTo y
  }

  def getReport(month: Int, year: Int): Future[Report] = {
    val baseDate = DateTime.now.withMonthOfYear(month).withYear(year)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val attendees = menuPerDayService.getAllOrderedByDateFilterDateRange(new Date(sDate.getMillis), new Date(eDate.getMillis)).flatMap {
      Future.traverse(_) { (menuPerDay: MenuPerDay) =>
        menuPerDayPerPersonService.getListOfPeopleByMenuPerDayForReport(menuPerDay)
      }
    }.map(_.flatten)
    attendees.map(menuAttendant => Report(menuAttendant.groupBy(_.date.toString).mapValues(_.map(_.name)).toSeq.sortBy { case (date, _) =>
      date
    }))
  }

  def getReportByLocationAndDate(month: Int, year: Int): Future[ReportByDateAndLocation] = {
    val baseDate = DateTime.now.withMonthOfYear(month).withYear(year)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val attendees: Future[Seq[MenuPerDayReportByDateAndLocation]] =
      menuPerDayService.getAllOrderedByDateFilterDateRange(new Date(sDate.getMillis), new Date(eDate.getMillis))
        .flatMap { menuPerDay =>
          Future.traverse(menuPerDay) { mpd: MenuPerDay =>
            menuPerDayPerPersonService.getListOfPeopleByMenuPerDayByLocationAndDateForReport(mpd)
          }
        }.map(_.flatten)
    attendees.map(attendees => {
      ReportByDateAndLocation(
        attendees.groupBy(attendee => (attendee.date, attendee.location))
          .mapValues(_.map(_.attendeeName)).toSeq.sortBy { case ((date, _), _) =>
          date
        }
      )
    })
  }

  def getReportForNotAttending(month: Int, year: Int): Future[Report] = {
    val baseDate = DateTime.now.withMonthOfYear(month).withYear(year)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val peopleNotAttending = menuPerDayService.getAllAvailableDatesWithinRange(new Date(sDate.getMillis), new Date(eDate.getMillis)).flatMap {
      Future.traverse(_) { date: Date =>
        menuPerDayPerPersonService.getNotAttendingByDate(date)
      }
    }.map(_.flatten)
    peopleNotAttending.map( people =>
      Report(people.groupBy(_.date.toString)
        .mapValues(_.map(_.name)).toSeq.sortBy { case (date, _) =>
          date
        }
      )
    )
  }

  def exportToExcel(report: Report, reportNotAttending: Report): Array[Byte] = {
    val workbook = new XSSFWorkbook
    val cellStyle = workbook.createCellStyle
    val font = workbook.createFont
    font.setBold(true)
    cellStyle.setFont(font)

    val out = new ByteArrayOutputStream
    try {
      report.usersPerDate.foreach { case (date, users) => {

        val sheet = workbook.createSheet(date)

        initializeSheet(sheet, date, users, cellStyle)
        writeSecondRow(sheet.createRow(2), "Attendees:", cellStyle, 0)
        writeUserData(sheet, users, 0)
      }}

      reportNotAttending.usersPerDate.foreach { case (date, users) =>
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
    firstRow.zipWithIndex.foreach { case (cellValue, index) =>
      val cell = row.createCell(index)
      cell.setCellValue(cellValue)

      if (cellValue == "Date:" || cellValue == "Total:") {
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
    users.zipWithIndex.foreach { case (user, index) =>
      val row = if(sheet.getRow(index + rowSkips) == null) sheet.createRow(index + rowSkips) else sheet.getRow(index + rowSkips) //scalastyle:ignore
      val cell = row.createCell(columnIndex)
      cell.setCellValue(user.toString)
    }
  }

}
