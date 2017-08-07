package lunatech.lunchplanner.models

case class Report(usersPerDate: Map[String, Seq[String]], total: Int)
