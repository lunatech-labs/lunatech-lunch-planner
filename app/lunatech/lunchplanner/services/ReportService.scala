package lunatech.lunchplanner.services

import java.io.ByteArrayOutputStream
import java.sql.Date

import javax.inject.Inject
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
    menuPerDayService: MenuPerDayService)(
    implicit val connection: DBConnection) {

  implicit def ordered: Ordering[Date] = new Ordering[Date] {
    override def compare(x: Date, y: Date): Int = x compareTo y
  }

  def getReportByLocationAndDate(month: Int,
                                 year: Int): Future[ReportByDateAndLocation] = {
    val baseDate = DateTime.now.withMonthOfYear(month).withYear(year)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val attendees: Future[Seq[MenuPerDayReportByDateAndLocation]] =
      menuPerDayService
        .getAllOrderedByDateFilterDateRangeWithDeleted(new Date(sDate.getMillis),
                                            new Date(eDate.getMillis))
        .flatMap { menuPerDay =>
          Future.traverse(menuPerDay) { mpd: MenuPerDay =>
            menuPerDayPerPersonService
              .getListOfPeopleByMenuPerDayByLocationAndDateForReport(mpd)
          }
        }
        .map(_.flatten)
    attendees.map(attendees => {
      ReportByDateAndLocation(
        attendees
          .groupBy(attendee => (attendee.date, attendee.location))
          .mapValues(_.map(_.attendeeName))
          .toSeq
          .sortBy {
            case ((date, _), _) =>
              date
          }
      )
    })
  }

  def getReportForNotAttending(month: Int, year: Int): Future[Report] = {
    val baseDate = DateTime.now.withMonthOfYear(month).withYear(year)
    val sDate = baseDate.withDayOfMonth(1)
    val eDate = sDate.plusMonths(1).minusDays(1)

    val peopleNotAttending = menuPerDayService
      .getAllAvailableDatesWithinRange(new Date(sDate.getMillis),
                                       new Date(eDate.getMillis))
      .flatMap {
        Future.traverse(_) { date: Date =>
          menuPerDayPerPersonService.getNotAttendingByDate(date)
        }
      }
      .map(_.flatten)
    peopleNotAttending.map(
      people =>
        Report(
          people
            .groupBy(_.date.toString)
            .mapValues(_.map(_.name))
            .toSeq
            .sortBy {
              case (date, _) =>
                date
            }))
  }

  def exportToExcel(report: ReportByDateAndLocation,
                    reportNotAttending: Report): Array[Byte] = {
    val workbook = new XSSFWorkbook
    val cellStyle = workbook.createCellStyle
    val font = workbook.createFont
    font.setBold(true)
    cellStyle.setFont(font)

    val out = new ByteArrayOutputStream
    try {
      writeReport(workbook, report, cellStyle)
      writeReportForNotAttending(workbook, reportNotAttending, cellStyle)
      workbook.write(out)
    } finally {
      out.close()
      workbook.close()
    }

    out.toByteArray
  }

  private def writeReport(workbook: XSSFWorkbook,
                          report: ReportByDateAndLocation,
                          cellStyle: CellStyle): Unit = {
    report.usersPerDateAndLocation
      .groupBy { case ((date, _), _) => date }
      .foreach {
        case (date, reportByDateAndLocations) =>
          val sheet = workbook.createSheet(date.toString)
          initializeSheet(sheet,
                          date.toString,
                          reportByDateAndLocations.flatMap {
                            case ((_, _), users) => users
                          }.size,
                          cellStyle)

          reportByDateAndLocations.zipWithIndex.foreach {
            case (((_, location), users), index) =>
              val row =
                if (sheet.getRow(StartingRow) == null)
                  sheet.createRow(StartingRow)
                else sheet.getRow(StartingRow)
              writeSecondRow(row, s"$location:", cellStyle, index)
              writeUserData(sheet, users, index)
          }
      }
  }

  private def writeReportForNotAttending(workbook: XSSFWorkbook,
                                         reportNotAttending: Report,
                                         cellStyle: CellStyle): Unit = {
    reportNotAttending.usersPerDate.foreach {
      case (date, users) =>
        Option(workbook.getSheet(date)) match {
          case Some(s) =>
            val columnSkips = 3
            writeSecondRow(s.getRow(StartingRow),
                           "Did not attend:",
                           cellStyle,
                           columnSkips)
            writeUserData(s, users, columnSkips)
          case None =>
            val sheet = workbook.createSheet(date)
            val firstColumn = 0
            initializeSheet(sheet, date, users.size, cellStyle)
            writeSecondRow(sheet.createRow(StartingRow),
                           "Did not attend:",
                           cellStyle,
                           firstColumn)
            writeUserData(sheet, users, firstColumn)
        }
    }
  }

  private def initializeSheet(sheet: XSSFSheet,
                              date: String,
                              totalUsers: Int,
                              cellStyle: CellStyle): Unit = {
    val row = sheet.createRow(0)
    val firstRow = Array("Date:", date, "", "Total Attendees:", s"$totalUsers")
    firstRow.zipWithIndex.foreach {
      case (cellValue, index) =>
        val cell = row.createCell(index)
        cell.setCellValue(cellValue)

        if (cellValue == "Date:" || cellValue == "Total Attendees:") {
          cell.setCellStyle(cellStyle)
        }
    }
  }

  private def writeSecondRow(row: XSSFRow,
                             cellValue: String,
                             cellStyle: CellStyle,
                             columnIndex: Int): Unit = {
    val secondRowCell = row.createCell(columnIndex)
    secondRowCell.setCellValue(cellValue)
    secondRowCell.setCellStyle(cellStyle)
  }

  private def writeUserData(sheet: XSSFSheet,
                            users: Seq[String],
                            columnIndex: Int): Unit = {
    val rowSkips = 3
    users.zipWithIndex.foreach {
      case (user, index) =>
        val row =
          if (sheet.getRow(index + rowSkips) == null)
            sheet.createRow(index + rowSkips)
          else sheet.getRow(index + rowSkips)
        val cell = row.createCell(columnIndex)
        cell.setCellValue(user.toString)
    }
  }

  val StartingRow = 2

}
