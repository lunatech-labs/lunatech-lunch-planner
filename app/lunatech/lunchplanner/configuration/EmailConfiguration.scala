package lunatech.lunchplanner.configuration

import play.api.Configuration

import javax.inject.Inject

case class MonthlyReportEmailConfiguration(
    subject: String,
    from: String,
    to: Seq[String]
)

class EmailConfiguration @Inject() (val configuration: Configuration) {
  def getMonthlyReportEmailConfiguration(): MonthlyReportEmailConfiguration =
    MonthlyReportEmailConfiguration(
      subject = configuration.get[String]("monthly-report-email.subject"),
      from = configuration.get[String]("monthly-report-email.from"),
      to = configuration.get[Seq[String]]("monthly-report-email.recipients")
    )
}
