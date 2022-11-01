package lunatech.lunchplanner.models

import lunatech.lunchplanner.models.Report.WeekNumber
import java.sql.Date

object Report {
  type WeekNumber = Int
  type Location   = String
  type Users      = Seq[String]
}

final case class ReportByDate(
    usersPerDate: Seq[(Date, WeekNumber, Report.Users)]
)

final case class ReportByDateAndLocation(
    usersPerDateAndLocation: Seq[
      ((Date, WeekNumber, Report.Location), Report.Users)
    ]
)

// for the users that did not attend a schedule. Currently not used
final case class ReportDate(month: Int, year: Int)
