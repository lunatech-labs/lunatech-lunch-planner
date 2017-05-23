package lunatech.lunchplanner.viewModels

import java.util.{Date, UUID}

import play.api.data.Form
import play.api.data.Forms.{date, mapping}

case class ReportForm(dateStart: Date, dateEnd: Date)

object ReportForm {
  val reportForm = Form(
    mapping(
      "dateStart" -> date(pattern = "dd-MM-yyyy"),
      "dateEnd" -> date(pattern = "dd-MM-yyyy")
    )(ReportForm.apply)(ReportForm.unapply)
  )
}

case class FilterReportForm(dateStart: Date, dateEnd: Date)

object FilterReportForm {
  val filterReportForm = Form(
    mapping(
      "dateStart" -> date(pattern = "dd-MM-yyyy"),
      "dateEnd" -> date(pattern = "dd-MM-yyyy")
    )(FilterReportForm.apply)(FilterReportForm.unapply)
  )
}
