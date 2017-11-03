package lunatech.lunchplanner.models

import java.sql.Date

case class Report(usersPerDate: Map[String, Seq[String]])
case class ReportByDateAndLocation(usersPerDateAndLocation: Map[(Date, String), Seq[String]])