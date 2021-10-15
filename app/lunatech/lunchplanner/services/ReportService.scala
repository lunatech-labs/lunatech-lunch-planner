package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models._
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.{ XSSFRow, XSSFSheet, XSSFWorkbook }

import java.io.ByteArrayOutputStream
import java.sql.Date
import java.time.{ LocalDateTime, ZoneOffset }
import javax.inject.Inject
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

  def getSortedReport(
      month: Int,
      year: Int): Future[List[(Date, ReportByDateAndLocation)]] = {
    getReportByLocationAndDate(month, year).map(repList => {
      repList.usersPerDateAndLocation
        .groupBy { case ((date, _), _) => date }
        .map(item => {
          (item._1, ReportByDateAndLocation(item._2))
        })
        .toList
        .sortWith((rep1, rep2) => {
          rep1._1.after(rep2._1)
        })
    })
  }

  def getReportByLocationAndDate(month: Int,
                                 year: Int): Future[ReportByDateAndLocation] = {
    val baseDate = LocalDateTime.now.withMonth(month).withYear(year)
    val startDate = baseDate.withDayOfMonth(1)
    val endDate = startDate.plusMonths(1).minusDays(1)

    type Location = String
    type Attendees = Seq[String]
    def groupAndSort(
        scheduleWithAttendees: Seq[MenuPerDayReportByDateAndLocation])
      : Seq[((Date, Location), Attendees)] = {
      scheduleWithAttendees
        .groupBy(schedule => (schedule.date, schedule.location))
        .view
        .mapValues(_.map(_.attendeeName))
        .toSeq
        .sortBy { case ((date, location), _) => (date, location) }
        .map {
          case (dateAndLocation, attendees) =>
            (dateAndLocation, attendees.sortBy(attendee => attendee))
        }
    }

    val sDateMillis = startDate.toInstant(ZoneOffset.UTC).toEpochMilli
    val eDateMillis = endDate.toInstant(ZoneOffset.UTC).toEpochMilli
    for {
      menuPerDayList <- menuPerDayService.getAllOrderedByDateFilterDateRange(
        new Date(sDateMillis),
        new Date(eDateMillis))
      attendeesPerSchedule <- Future.traverse(menuPerDayList)(
        mpd =>
          menuPerDayPerPersonService
            .getListOfPeopleByMenuPerDayByLocationAndDateForReport(mpd))
      scheduleWithAttendees = attendeesPerSchedule
        .flatten[MenuPerDayReportByDateAndLocation]
    } yield
      ReportByDateAndLocation(
        usersPerDateAndLocation = groupAndSort(scheduleWithAttendees))
  }

  def getReportForNotAttending(month: Int, year: Int): Future[Report] = {
    val baseDate = LocalDateTime.now.withMonth(month).withYear(year)
    val startDate = baseDate.withDayOfMonth(1)
    val endDate = startDate.plusMonths(1).minusDays(1)

    type DateString = String
    type Names = Seq[String]
    def groupAndSort(
        people: Seq[MenuPerDayReport]): Seq[(DateString, Names)] = {
      people.groupBy(_.date.toString).view.mapValues(_.map(_.name)).toSeq.sortBy {
        case (date, _) => date
      }
    }

    val startDateMillis = startDate.toInstant(ZoneOffset.UTC).toEpochMilli
    val endDateMillis = endDate.toInstant(ZoneOffset.UTC).toEpochMilli
    for {
      dates <- menuPerDayService.getAllAvailableDatesWithinRange(
        new Date(startDateMillis),
        new Date(endDateMillis))
      peopleNotAttending <- Future.traverse(dates)(date =>
        menuPerDayPerPersonService.getNotAttendingByDate(date))
      people = peopleNotAttending.flatten[MenuPerDayReport]
    } yield Report(usersPerDate = groupAndSort(people))

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
    def getTotalUsers(
        reportByDateAndLocations: Seq[((Date, Report.Location), Report.Users)])
      : Int = {
      reportByDateAndLocations.flatMap { case ((_, _), users) => users }.size
    }

    if (report.usersPerDateAndLocation.isEmpty) {
      workbook.createSheet("No attendants")
    } else {
      report.usersPerDateAndLocation
        .groupBy { case ((date, _), _) => date }
        .foreach {
          case (date, reportByDateAndLocations) =>
            val sheet = workbook.createSheet(date.toString)
            initializeSheet(
              sheet = sheet,
              date = date.toString,
              totalUsers = getTotalUsers(reportByDateAndLocations),
              cellStyle = cellStyle)

            reportByDateAndLocations.zipWithIndex.foreach {
              case (((_, location), users), index) =>
                val row = Option(sheet.getRow(StartingRow)) match {
                  case Some(r) => r
                  case None    => sheet.createRow(StartingRow)
                }
                writeSecondRow(row, s"$location:", cellStyle, index)
                writeUserData(sheet, users, index)
            }
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
            writeSecondRow(row = s.getRow(StartingRow),
                           cellValue = "Did not attend:",
                           cellStyle = cellStyle,
                           columnIndex = columnSkips)
            writeUserData(sheet = s, users = users, columnIndex = columnSkips)
          case None =>
            val sheet = workbook.createSheet(date)
            val firstColumn = 0
            initializeSheet(sheet = sheet,
                            date = date,
                            totalUsers = users.size,
                            cellStyle = cellStyle)
            writeSecondRow(row = sheet.createRow(StartingRow),
                           cellValue = "Did not attend:",
                           cellStyle = cellStyle,
                           columnIndex = firstColumn)
            writeUserData(sheet = sheet,
                          users = users,
                          columnIndex = firstColumn)
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
        val row = Option(sheet.getRow(index + rowSkips)) match {
          case Some(r) => r
          case None    => sheet.createRow(index + rowSkips)
        }

        val cell = row.createCell(columnIndex)
        cell.setCellValue(user.toString)
    }
  }

  val StartingRow = 2
}
