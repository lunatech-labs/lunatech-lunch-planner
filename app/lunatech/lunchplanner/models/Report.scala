package lunatech.lunchplanner.models

import java.sql.Date

object Report {
  type Location = String
  type Users = Seq[String]
}

final case class Report(usersPerDate: Seq[(String, Report.Users)])

final case class ReportByDateAndLocation(usersPerDateAndLocation: Seq[((Date, Report.Location), Report.Users)])

final case class ReportDate(month: Int, year: Int)
