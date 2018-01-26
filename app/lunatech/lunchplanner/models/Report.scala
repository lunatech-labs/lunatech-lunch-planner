package lunatech.lunchplanner.models

import java.sql.Date

object Report {
  type Location = String
  type Users = Seq[String]
}

case class Report(usersPerDate: Seq[(String, Report.Users)])

case class ReportByDateAndLocation(usersPerDateAndLocation: Seq[((Date, Report.Location), Report.Users)])

case class ReportDate(month: Int, year: Int)
