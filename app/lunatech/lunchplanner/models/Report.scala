package lunatech.lunchplanner.models

import java.sql.Date

case class Report(usersPerDate: Seq[(String, Seq[String])])
case class ReportByDateAndLocation(usersPerDateAndLocation: Seq[((Date, String), Seq[String])])

case class ReportDate(month: Int, year: Int)
